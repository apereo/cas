package org.jasig.cas.ticket.registry;

import org.jasig.cas.support.oauth.ticket.OAuthToken;
import org.jasig.cas.support.oauth.ticket.accesstoken.AccessToken;
import org.jasig.cas.support.oauth.ticket.code.OAuthCode;
import org.jasig.cas.support.oauth.ticket.code.OAuthCodeImpl;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.ServiceTicketImpl;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;
import org.jasig.cas.ticket.registry.support.LockingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * JPA implementation of a CAS {@link TicketRegistry}. This implementation of
 * ticket registry is suitable for HA environments.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.2.1
 */
@Component("jpaTicketRegistry")
public final class JpaTicketRegistry extends AbstractTicketRegistry {

    @Autowired
    @Qualifier("jpaLockingStrategy")
    private LockingStrategy jpaLockingStrategy;

    @Value("${ticketreg.database.jpa.locking.tgt.enabled:true}")
    private boolean lockTgt = true;

    @NotNull
    @PersistenceContext(unitName = "ticketEntityManagerFactory")
    private EntityManager entityManager;

    @Override
    public void updateTicket(final Ticket ticket) {
        entityManager.merge(ticket);
        logger.debug("Updated ticket [{}].", ticket);
    }

    @Override
    public void addTicket(final Ticket ticket) {
        entityManager.persist(ticket);
        logger.debug("Added ticket [{}] to registry.", ticket);
    }

    /**
     * Removes the ticket.
     *
     * @param ticket the ticket
     */
    private boolean removeTicket(final Ticket ticket) {
        try {
            final ZonedDateTime creationDate = ticket.getCreationTime();
            logger.debug("Removing Ticket [{}] created: {}", ticket, creationDate.toString());
            entityManager.remove(ticket);
            return true;
        } catch (final Exception e) {
            logger.error("Error removing {} from registry.", ticket.getId(), e);
        }
        return false;
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        return getRawTicket(ticketId);
    }

    /**
     * Gets the ticket from the database, as is.
     *
     * @param ticketId the ticket id
     * @return the raw ticket
     */
    private Ticket getRawTicket(final String ticketId) {
        try {
            if (ticketId.startsWith(TicketGrantingTicket.PREFIX)
                    || ticketId.startsWith(ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX)) {
                // There is no need to distinguish between TGTs and PGTs since PGTs inherit from TGTs
                return entityManager.find(TicketGrantingTicketImpl.class, ticketId,
                        lockTgt ? LockModeType.PESSIMISTIC_WRITE : null);
            } else if (ticketId.startsWith(OAuthCode.PREFIX) || ticketId.startsWith(AccessToken.PREFIX)) {
                return entityManager.find(OAuthCodeImpl.class, ticketId);
            }

            return entityManager.find(ServiceTicketImpl.class, ticketId);
        } catch (final Exception e) {
            logger.error("Error getting ticket {} from registry.", ticketId, e);
        }
        return null;
    }

    @Override
    public Collection<Ticket> getTickets() {
        final List<TicketGrantingTicketImpl> tgts = entityManager
                .createQuery("select t from TicketGrantingTicketImpl t", TicketGrantingTicketImpl.class)
                .getResultList();
        final List<ServiceTicketImpl> sts = entityManager
                .createQuery("select s from ServiceTicketImpl s", ServiceTicketImpl.class)
                .getResultList();

        final List<Ticket> tickets = new ArrayList<>(tgts);
        tickets.addAll(sts);

        return tickets;
    }

    @Override
    protected boolean needsCallback() {
        return false;
    }

    @Override
    public long sessionCount() {
        return countToLong(entityManager.createQuery(
                "select count(t) from TicketGrantingTicketImpl t").getSingleResult());
    }

    @Override
    public long serviceTicketCount() {
        return countToLong(entityManager.createQuery("select count(t) from ServiceTicketImpl t").getSingleResult());
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        final Ticket ticket = getTicket(ticketId);
        final int failureCount;

        if (ticket instanceof OAuthToken) {
            failureCount = deleteOAuthTokens(ticketId);
        } else if (ticket instanceof ServiceTicket) {
            failureCount = deleteServiceTickets(ticketId);
        } else if (ticket instanceof TicketGrantingTicket) {
            failureCount = deleteTicketGrantingTickets(ticketId);
        } else {
            throw new IllegalArgumentException("Invalid ticket type with id " + ticketId);
        }
        return failureCount == 0;
    }

    <T extends Ticket> List<T> getTicketQueryResultList(final String ticketId, final String query, final Class<T> clazz) {
        return entityManager.createQuery(query, clazz)
                .setParameter("id", ticketId)
                .getResultList();
    }

    private int deleteOAuthTokens(final String ticketId) {
        final List<OAuthCodeImpl> oAuthCodeImpls = getTicketQueryResultList(ticketId,
                "select o from OAuthCodeImpl o where o.id = :id", OAuthCodeImpl.class);
        return deleteTicketsFromResultList(oAuthCodeImpls);
    }

    private int deleteServiceTickets(final String ticketId) {
        final List<ServiceTicketImpl> serviceTicketImpls = getTicketQueryResultList(ticketId,
                "select s from ServiceTicketImpl s where s.id = :id", ServiceTicketImpl.class);
        return deleteTicketsFromResultList(serviceTicketImpls);
    }

    private int deleteTicketsFromResultList(final List<? extends Ticket> serviceTicketImpls) {
        int failureCount = 0;
        for (final Ticket serviceTicketImpl : serviceTicketImpls) {
            if (!removeTicket(serviceTicketImpl)) {
                failureCount++;
            }
        }
        return failureCount;
    }

    private int deleteTicketGrantingTickets(final String ticketId) {
        int failureCount = 0;

        final List<ServiceTicketImpl> serviceTicketImpls = getTicketQueryResultList(ticketId,
                "select s from ServiceTicketImpl s where s.ticketGrantingTicket.id = :id", ServiceTicketImpl.class);
        failureCount += deleteTicketsFromResultList(serviceTicketImpls);

        List<TicketGrantingTicketImpl> ticketGrantingTicketImpls = getTicketQueryResultList(ticketId,
                "select t from TicketGrantingTicketImpl t where t.ticketGrantingTicket.id = :id", TicketGrantingTicketImpl.class);
        failureCount += deleteTicketsFromResultList(ticketGrantingTicketImpls);

        ticketGrantingTicketImpls = getTicketQueryResultList(ticketId,
                "select t from TicketGrantingTicketImpl t where t.id = :id", TicketGrantingTicketImpl.class);
        failureCount += deleteTicketsFromResultList(ticketGrantingTicketImpls);

        return failureCount;
    }

    /**
     * Count the result into a numeric value.
     *
     * @param result the result
     * @return the int
     */
    private static long countToLong(final Object result) {
        return ((Number) result).longValue();
    }

    @Override
    protected void postCleanupTickets() {
        logger.debug("Releasing ticket cleanup lock.");
        this.jpaLockingStrategy.release();
        logger.info("Finished ticket cleanup.");
    }

    @Override
    protected boolean preCleanupTickets() {
        logger.debug("Attempting to acquire ticket cleanup lock.");
        if (!this.jpaLockingStrategy.acquire()) {
            logger.warn("Could not obtain lock. Aborting cleanup.");
            return false;
        }
        logger.debug("Acquired lock. Proceeding with cleanup.");
        return super.preCleanupTickets();
    }
}
