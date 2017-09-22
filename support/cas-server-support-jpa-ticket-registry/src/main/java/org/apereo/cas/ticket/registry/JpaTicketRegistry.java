package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketGrantingTicket;
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
        final Collection<TicketDefinition> tkts = this.ticketCatalog.findAll();
        final AtomicLong count = new AtomicLong();
        tkts.forEach(t -> {
            final String entityName = getTicketEntityName(t);
            final Query query = entityManager.createQuery("delete from " + entityName);
            LOGGER.debug("Deleting ticket entity [{}]", entityName);
            count.addAndGet(query.executeUpdate());
        });
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
            final TicketDefinition tkt = this.ticketCatalog.find(ticketId);
            return this.entityManager.find(tkt.getImplementationClass(), ticketId, this.lockType);
        } catch (final Exception e) {
            LOGGER.error("Error getting ticket [{}] from registry.", ticketId, e);
        }
        return null;
    }

    @Override
    public Collection<Ticket> getTickets() {
        final Collection<TicketDefinition> tkts = this.ticketCatalog.findAll();
        final List<Ticket> tickets = new ArrayList<>();
        tkts.forEach(t -> {
            final Query query = this.entityManager.createQuery("select t from " + getTicketEntityName(t) + " t", t.getImplementationClass());
            tickets.addAll(query.getResultList());
        });
        return tickets;
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
