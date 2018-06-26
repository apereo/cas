package org.apereo.cas.ticket.registry;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.hibernate.LockOptions;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Collection;
import java.util.List;
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
public class JpaTicketRegistry extends AbstractTicketRegistry {
    private static final int STREAM_BATCH_SIZE = 100;

    private final TicketCatalog ticketCatalog;
    private final LockModeType lockType;

    @PersistenceContext(unitName = "ticketEntityManagerFactory")
    private transient EntityManager entityManager;

    public JpaTicketRegistry(final LockModeType lockType, final TicketCatalog ticketCatalog) {
        this.lockType = lockType;
        this.ticketCatalog = ticketCatalog;
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        this.entityManager.merge(ticket);
        LOGGER.debug("Updated ticket [{}].", ticket);
        return ticket;
    }

    @Override
    public void addTicket(final Ticket ticket) {
        this.entityManager.persist(ticket);
        LOGGER.debug("Added ticket [{}] to registry.", ticket);
    }

    @Override
    public long deleteAll() {
        return this.ticketCatalog.findAll().stream()
            .map(JpaTicketRegistry::getTicketEntityName)
            .map(entityName -> entityManager.createQuery(String.format("delete from %s", entityName)))
            .mapToLong(Query::executeUpdate)
            .sum();
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        try {
            final var tkt = ticketCatalog.find(ticketId);
            final var sql = String.format("select t from %s t where t.id = :id", getTicketEntityName(tkt));
            final TypedQuery<? extends Ticket> query = entityManager.createQuery(sql, tkt.getImplementationClass());
            query.setParameter("id", ticketId);
            query.setLockMode(this.lockType);
            final Ticket result = query.getSingleResult();
            if (result != null && result.isExpired()) {
                LOGGER.debug("Ticket [{}] has expired and will be removed from the database", result.getId());
                return null;
            }
            return result;
        } catch (final Exception e) {
            LOGGER.error("Error getting ticket [{}] from registry.", ticketId, e);
        }
        return null;
    }

    @Override
    public Collection<Ticket> getTickets() {
        return this.ticketCatalog.findAll()
            .stream()
            .map(t -> {
                final var sql = String.format("select t from %s t", getTicketEntityName(t));
                final TypedQuery<? extends Ticket> query = entityManager.createQuery(sql, t.getImplementationClass());
                query.setLockMode(this.lockType);
                return query;
            })
            .map(TypedQuery::getResultList)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    /**
     * Gets a stream which loads tickets from the database in batches instead of all at once to prevent OOM situations.
     * <p>
     * This method purposefully doesn't lock any rows, because the stream traversing can take an indeterminate
     * amount of time, and logging in to an application with an existing TGT will update the TGT row in the database.
     *
     * @return {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Stream<Ticket> getTicketsStream() {
        return this.ticketCatalog.findAll()
            .stream()
            .map(t -> {
                final var sql = String.format("select t from %s t", getTicketEntityName(t));
                final var query = (org.hibernate.query.Query<Ticket>) entityManager.createQuery(sql, t.getImplementationClass());
                query.setFetchSize(STREAM_BATCH_SIZE);
                query.setLockOptions(LockOptions.NONE);
                return query;
            })
            .flatMap(org.hibernate.query.Query::stream);
    }

    @Override
    public long sessionCount() {
        final var md = this.ticketCatalog.find(TicketGrantingTicket.PREFIX);
        final var sql = String.format("select count(t) from %s t", getTicketEntityName(md));
        final var query = this.entityManager.createQuery(sql);
        return countToLong(query.getSingleResult());
    }

    @Override
    public long serviceTicketCount() {
        final var md = this.ticketCatalog.find(ServiceTicket.PREFIX);
        final var sql = String.format("select count(t) from %s t", getTicketEntityName(md));
        final var query = this.entityManager.createQuery(sql);
        return countToLong(query.getSingleResult());
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        var totalCount = 0;
        final var md = this.ticketCatalog.find(ticketId);

        if (md.getProperties().isCascade()) {
            totalCount = deleteTicketGrantingTickets(ticketId);
        } else {
            final var ticketEntityName = getTicketEntityName(md);
            try {
                final var sql = String.format("delete from %s o where o.id = :id", ticketEntityName);
                final var query = entityManager.createQuery(sql);
                query.setParameter("id", ticketId);
                totalCount = query.executeUpdate();
            } catch (final EntityNotFoundException e) {
                LOGGER.debug("Entity [{}] for ticket id [{}] is not found in the database and may have already been deleted",
                    ticketEntityName, ticketId);
                LOGGER.trace(e.getMessage(), e);
            }
        }
        return totalCount != 0;
    }

    private static String getTicketEntityName(final TicketDefinition tk) {
        return tk.getImplementationClass().getSimpleName();
    }

    /**
     * Delete ticket granting tickets int.
     *
     * @param ticketId the ticket id
     * @return the int
     */
    private int deleteTicketGrantingTickets(final String ticketId) {
        var totalCount = 0;

        final var st = this.ticketCatalog.find(ServiceTicket.PREFIX);

        final var sql1 = String.format("delete from %s s where s.ticketGrantingTicket.id = :id", getTicketEntityName(st));
        var query = entityManager.createQuery(sql1);
        query.setParameter("id", ticketId);
        totalCount += query.executeUpdate();

        final var tgt = this.ticketCatalog.find(TicketGrantingTicket.PREFIX);
        final var sql2 = String.format("delete from %s s where s.ticketGrantingTicket.id = :id", getTicketEntityName(tgt));
        query = entityManager.createQuery(sql2);
        query.setParameter("id", ticketId);
        totalCount += query.executeUpdate();

        final var sql3 = String.format("delete from %s t where t.id = :id", getTicketEntityName(tgt));
        query = entityManager.createQuery(sql3);
        query.setParameter("id", ticketId);
        totalCount += query.executeUpdate();

        return totalCount;
    }

    private static long countToLong(final Object result) {
        return ((Number) result).longValue();
    }
}
