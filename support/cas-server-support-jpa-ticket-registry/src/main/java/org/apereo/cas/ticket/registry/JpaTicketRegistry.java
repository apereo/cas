package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketMetadata;
import org.apereo.cas.ticket.TicketMetadataRegistrationPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.apereo.cas.ticket.TicketMetadata.TicketMetadataProperties.*;

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

    private final TicketMetadataRegistrationPlan ticketMetadataRegistrationPlan;
    private final LockModeType lockType;

    @PersistenceContext(unitName = "ticketEntityManagerFactory")
    private EntityManager entityManager;

    public JpaTicketRegistry(final LockModeType lockType, final TicketMetadataRegistrationPlan ticketMetadataRegistrationPlan) {
        this.lockType = lockType;
        this.ticketMetadataRegistrationPlan = ticketMetadataRegistrationPlan;
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
        final Collection<TicketMetadata> tkts = this.ticketMetadataRegistrationPlan.findAllTicketMetadata();
        final AtomicLong count = new AtomicLong();
        tkts.forEach(t -> count.addAndGet(entityManager.createQuery("delete from " + getTicketEntityName(t)).executeUpdate()));
        return count.get();
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
            final TicketMetadata tkt = this.ticketMetadataRegistrationPlan.findTicketMetadata(ticketId);
            return this.entityManager.find(tkt.getImplementationClass(), ticketId, this.lockType);
        } catch (final Exception e) {
            LOGGER.error("Error getting ticket [{}] from registry.", ticketId, e);
        }
        return null;
    }

    @Override
    public Collection<Ticket> getTickets() {
        final Collection<TicketMetadata> tkts = this.ticketMetadataRegistrationPlan.findAllTicketMetadata();
        final List<Ticket> tickets = new ArrayList<>();
        tkts.forEach(t -> {
            final Query query = this.entityManager.createQuery("select t from " + getTicketEntityName(t) + " t", t.getImplementationClass());
            tickets.addAll(query.getResultList());
        });
        return tickets;
    }

    @Override
    public long sessionCount() {
        final TicketMetadata md = this.ticketMetadataRegistrationPlan.findTicketMetadata(TicketGrantingTicket.PREFIX);
        return countToLong(this.entityManager.createQuery("select count(t) from " + getTicketEntityName(md) + " t").getSingleResult());
    }

    @Override
    public long serviceTicketCount() {
        final TicketMetadata md = this.ticketMetadataRegistrationPlan.findTicketMetadata(ServiceTicket.PREFIX);
        return countToLong(this.entityManager.createQuery("select count(t) from " + getTicketEntityName(md) + " t").getSingleResult());
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        final int failureCount;
        final TicketMetadata md = this.ticketMetadataRegistrationPlan.findTicketMetadata(ticketId);

        if (md.getPropertyAsBoolean(CASCADE_TICKET)) {
            failureCount = deleteTicketGrantingTickets(ticketId);
        } else {
            final Query query = entityManager.createQuery("delete from " + getTicketEntityName(md) + " o where o.id = :id");
            query.setParameter("id", ticketId);
            failureCount = query.executeUpdate();
        }
        return failureCount == 0;
    }

    private String getTicketEntityName(final TicketMetadata tk) {
        return tk.getImplementationClass().getSimpleName();
    }

    /**
     * Delete ticket granting tickets int.
     *
     * @param ticketId the ticket id
     * @return the int
     */
    private int deleteTicketGrantingTickets(final String ticketId) {
        int failureCount = 0;

        final TicketMetadata st = this.ticketMetadataRegistrationPlan.findTicketMetadata(ServiceTicket.PREFIX);

        Query query = entityManager.createQuery("delete from " + getTicketEntityName(st) + " s where s.ticketGrantingTicket.id = :id");
        query.setParameter("id", ticketId);
        failureCount += query.executeUpdate();

        final TicketMetadata tgt = this.ticketMetadataRegistrationPlan.findTicketMetadata(TicketGrantingTicket.PREFIX);
        query = entityManager.createQuery("delete from " + getTicketEntityName(tgt) + " t where t.ticketGrantingTicket.id = :id");
        query.setParameter("id", ticketId);
        failureCount += query.executeUpdate();

        query = entityManager.createQuery("delete from " + getTicketEntityName(tgt) + " t where t.id = :id");
        query.setParameter("id", ticketId);
        failureCount += query.executeUpdate();

        return failureCount;
    }

    private static long countToLong(final Object result) {
        return ((Number) result).longValue();
    }
}
