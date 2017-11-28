package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.hibernate.LockOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
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
@Transactional(transactionManager = "ticketTransactionManager", readOnly = false)
public class JpaTicketRegistry extends AbstractTicketRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(JpaTicketRegistry.class);

    private static final int STREAM_BATCH_SIZE = 100;

    private final TicketCatalog ticketCatalog;
    private final LockModeType lockType;

    @PersistenceContext(unitName = "ticketEntityManagerFactory")
    private EntityManager entityManager;

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
                .map(entityName -> entityManager.createQuery("delete from " + entityName))
                .mapToLong(Query::executeUpdate)
                .sum();
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        return getRawTicket(ticketId);
    }

    /**
     * Gets the ticket from the database, as is.
     * In removals, there is no need to distinguish between TGTs and PGTs since PGTs inherit from TGTs
     *
     * @param ticketId the ticket id
     * @return the raw ticket
     */
    public Ticket getRawTicket(final String ticketId) {
        try {
            final TicketDefinition tkt = this.ticketCatalog.find(ticketId);
            return this.entityManager.find(tkt.getImplementationClass(), ticketId, this.lockType);
        } catch (final Exception e) {
            LOGGER.error("Error getting ticket [{}] from registry.", ticketId, e);
        }
        return null;
    }

    @Override
    public Collection<Ticket> getTickets() {
        return this.ticketCatalog.findAll().stream()
                .map(t -> this.entityManager.createQuery("select t from " + getTicketEntityName(t) + " t", t.getImplementationClass()))
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
        return this.ticketCatalog.findAll().stream()
                .map(t -> this.entityManager.createQuery("select t from " + getTicketEntityName(t) + " t", t.getImplementationClass()))
                // Unwrap to Hibernate Query, which supports streams
                .map(q -> {
                    final org.hibernate.query.Query<Ticket> hq = (org.hibernate.query.Query<Ticket>) q.unwrap(org.hibernate.query.Query.class);
                    hq.setFetchSize(STREAM_BATCH_SIZE);
                    hq.setLockOptions(LockOptions.NONE);
                    return hq;
                })
                .flatMap(org.hibernate.query.Query::stream);
    }

    @Override
    public long sessionCount() {
        final TicketDefinition md = this.ticketCatalog.find(TicketGrantingTicket.PREFIX);
        return countToLong(this.entityManager.createQuery("select count(t) from " + getTicketEntityName(md) + " t").getSingleResult());
    }

    @Override
    public long serviceTicketCount() {
        final TicketDefinition md = this.ticketCatalog.find(ServiceTicket.PREFIX);
        return countToLong(this.entityManager.createQuery("select count(t) from " + getTicketEntityName(md) + " t").getSingleResult());
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        final int totalCount;
        final TicketDefinition md = this.ticketCatalog.find(ticketId);

        if (md.getProperties().isCascade()) {
            totalCount = deleteTicketGrantingTickets(ticketId);
        } else {
            final Query query = entityManager.createQuery("delete from " + getTicketEntityName(md) + " o where o.id = :id");
            query.setParameter("id", ticketId);
            totalCount = query.executeUpdate();
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
        int totalCount = 0;

        final TicketDefinition st = this.ticketCatalog.find(ServiceTicket.PREFIX);

        Query query = entityManager.createQuery("delete from " + getTicketEntityName(st) + " s where s.ticketGrantingTicket.id = :id");

        query.setParameter("id", ticketId);
        totalCount += query.executeUpdate();

        final TicketDefinition tgt = this.ticketCatalog.find(TicketGrantingTicket.PREFIX);
        query = entityManager.createQuery("delete from " + getTicketEntityName(tgt) + " t where t.ticketGrantingTicket.id = :id");
        query.setParameter("id", ticketId);
        totalCount += query.executeUpdate();

        query = entityManager.createQuery("delete from " + getTicketEntityName(tgt) + " t where t.id = :id");
        query.setParameter("id", ticketId);
        totalCount += query.executeUpdate();

        return totalCount;
    }
    
    private static long countToLong(final Object result) {
        return ((Number) result).longValue();
    }
}
