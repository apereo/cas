package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketGrantingTicket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.LockOptions;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * JPA implementation of a CAS {@link TicketRegistry}. This implementation of
 * ticket registry is suitable for HA environments.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.2.1
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(transactionManager = "ticketTransactionManager")
@Slf4j
@RequiredArgsConstructor
public class JpaTicketRegistry extends AbstractTicketRegistry {
    private static final int STREAM_BATCH_SIZE = 100;

    private final LockModeType lockType;

    private final TicketCatalog ticketCatalog;

    @PersistenceContext(unitName = "ticketEntityManagerFactory")
    private transient EntityManager entityManager;

    @Override
    public void addTicket(final Ticket ticket) {
        val encodeTicket = encodeTicket(ticket);
        this.entityManager.persist(encodeTicket);
        LOGGER.debug("Added ticket [{}] to registry.", encodeTicket);
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        try {
            val encTicketId = encodeTicketId(ticketId);
            if (StringUtils.isBlank(encTicketId)) {
                return null;
            }

            val tkt = ticketCatalog.find(ticketId);
            val sql = String.format("SELECT t FROM %s t WHERE t.id = :id", getTicketEntityName(tkt));
            val query = entityManager.createQuery(sql, getTicketImplementationClass(tkt));
            query.setParameter("id", encTicketId);
            query.setLockMode(this.lockType);
            val ticket = query.getSingleResult();
            val result = decodeTicket(ticket);
            if (predicate.test(result)) {
                return result;
            }
            return null;
        } catch (final NoResultException e) {
            LOGGER.debug("No record could be found for ticket [{}]", ticketId);
        } catch (final Exception e) {
            LOGGER.error("Error getting ticket [{}] from registry.", ticketId, e);
        }
        return null;
    }

    @Override
    public long deleteAll() {
        return this.ticketCatalog.findAll()
            .stream()
            .map(this::getTicketEntityName)
            .map(entityName -> entityManager.createQuery(String.format("DELETE FROM %s", entityName)))
            .mapToLong(Query::executeUpdate)
            .sum();
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
        if (isCipherExecutorEnabled()) {
            val sql = String.format("SELECT t FROM %s t", EncodedTicket.class.getSimpleName());
            val query = (org.hibernate.query.Query<Ticket>) entityManager.createQuery(sql, Ticket.class);
            query.setLockMode(this.lockType);
            return query
                .getResultStream()
                .map(this::decodeTicket)
                .collect(Collectors.toList());
        }

        return this.ticketCatalog.findAll()
            .stream()
            .map(t -> {
                val sql = String.format("SELECT t FROM %s t", getTicketEntityName(t));
                val query = entityManager.createQuery(sql, getTicketImplementationClass(t));
                query.setLockMode(this.lockType);
                return query;
            })
            .map(TypedQuery::getResultList)
            .flatMap(List::stream)
            .map(this::decodeTicket)
            .collect(Collectors.toList());
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        LOGGER.trace("Updating ticket [{}]", ticket);
        val encodeTicket = this.encodeTicket(ticket);
        this.entityManager.merge(encodeTicket);
        LOGGER.debug("Updated ticket [{}].", encodeTicket);
        return encodeTicket;
    }

    /**
     * Gets a stream which loads tickets from the database in batches instead of all at once to prevent OOM situations.
     * <p>
     * This method purposefully doesn't lock any rows, because the stream traversing can take an indeterminate
     * amount of time, and logging in to an application with an existing TGT will update the TGT row in the database.
     *
     * @return tickets
     */
    @Override
    public Stream<? extends Ticket> getTicketsStream() {
        if (isCipherExecutorEnabled()) {
            val sql = String.format("SELECT t FROM %s t", EncodedTicket.class.getSimpleName());
            val query = (org.hibernate.query.Query<Ticket>) entityManager.createQuery(sql, Ticket.class);
            query.setFetchSize(STREAM_BATCH_SIZE);
            query.setLockOptions(LockOptions.NONE);
            return query
                .stream()
                .map(this::decodeTicket);
        }

        return this.ticketCatalog.findAll()
            .stream()
            .map(t -> {
                val sql = String.format("SELECT t FROM %s t", getTicketEntityName(t));
                val query = (org.hibernate.query.Query<Ticket>) entityManager.createQuery(sql, getTicketImplementationClass(t));
                query.setFetchSize(STREAM_BATCH_SIZE);
                query.setLockOptions(LockOptions.NONE);
                return query;
            })
            .flatMap(org.hibernate.query.Query::stream)
            .map(this::decodeTicket);
    }

    @Override
    public long sessionCount() {
        if (isCipherExecutorEnabled()) {
            return getTicketsStream()
                .filter(ticket -> ticket instanceof TicketGrantingTicket)
                .count();
        }
        val md = this.ticketCatalog.find(TicketGrantingTicket.PREFIX);
        val sql = String.format("SELECT COUNT(t) FROM %s t", getTicketEntityName(md));
        val query = this.entityManager.createQuery(sql);
        return countToLong(query.getSingleResult());
    }

    @Override
    public long serviceTicketCount() {
        if (isCipherExecutorEnabled()) {
            return getTicketsStream()
                .filter(ticket -> ticket instanceof ServiceTicket)
                .count();
        }
        val md = this.ticketCatalog.find(ServiceTicket.PREFIX);
        val sql = String.format("SELECT COUNT(t) FROM %s t", getTicketEntityName(md));
        val query = this.entityManager.createQuery(sql);
        return countToLong(query.getSingleResult());
    }

    /**
     * Delete a ticket by its identifier.
     * Simple call to the super method to force a transaction to be started in case of a direct call.
     *
     * @param ticketId the ticket identifier
     * @return the number of tickets deleted including children.
     */
    @Override
    public int deleteTicket(final String ticketId) {
        return super.deleteTicket(ticketId);
    }

    @Override
    public boolean deleteSingleTicket(final String ticketIdToDelete) {
        val encTicketId = encodeTicketId(ticketIdToDelete);

        var totalCount = 0;
        val md = this.ticketCatalog.find(ticketIdToDelete);

        if (md.getProperties().isCascadeRemovals() && !isCipherExecutorEnabled()) {
            totalCount = deleteTicketGrantingTickets(encTicketId);
        } else {
            val ticketEntityName = getTicketEntityName(md);
            try {
                val sql = String.format("DELETE FROM %s o WHERE o.id = :id", ticketEntityName);
                val query = entityManager.createQuery(sql);
                query.setParameter("id", encTicketId);
                totalCount = query.executeUpdate();
            } catch (final EntityNotFoundException e) {
                LOGGER.debug("Entity [{}] for ticket id [{}] is not found and may have already been deleted", ticketEntityName, encTicketId);
                LOGGER.trace(e.getMessage(), e);
            }
        }
        return totalCount != 0;
    }

    private static long countToLong(final Object result) {
        return ((Number) result).longValue();
    }

    /**
     * Delete ticket granting tickets.
     *
     * @param ticketId the ticket id
     * @return the total count
     */
    private int deleteTicketGrantingTickets(final String ticketId) {
        var totalCount = this.ticketCatalog.findAll()
            .stream()
            .filter(defn -> !defn.getProperties().isExcludeFromCascade())
            .mapToInt(defn -> {
                try {
                    val sql = String.format("DELETE FROM %s s WHERE s.ticketGrantingTicket.id = :id", getTicketEntityName(defn));
                    LOGGER.trace("Creating query [{}]", sql);
                    val query = entityManager.createQuery(sql);
                    query.setParameter("id", ticketId);
                    return query.executeUpdate();
                } catch (final Exception e) {
                    LOGGER.trace(e.getMessage(), e);
                }
                return 0;
            })
            .sum();

        val tgt = this.ticketCatalog.find(TicketGrantingTicket.PREFIX);
        val sql = String.format("DELETE FROM %s t WHERE t.id = :id", getTicketEntityName(tgt));
        val query = entityManager.createQuery(sql);
        query.setParameter("id", ticketId);
        totalCount += query.executeUpdate();
        return totalCount;
    }

    private Class<? extends Ticket> getTicketImplementationClass(final TicketDefinition tk) {
        if (isCipherExecutorEnabled()) {
            return EncodedTicket.class;
        }
        return tk.getImplementationClass();
    }

    private String getTicketEntityName(final TicketDefinition tk) {
        return getTicketImplementationClass(tk).getSimpleName();
    }
}
