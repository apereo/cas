package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.OAuthToken;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.code.OAuthCodeImpl;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketImpl;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
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
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(transactionManager = "ticketTransactionManager", readOnly = false)
public class JpaTicketRegistry extends AbstractTicketRegistry {

    private static final String TABLE_OAUTH_TICKETS = OAuthCodeImpl.class.getSimpleName();
    private static final String TABLE_SERVICE_TICKETS = ServiceTicketImpl.class.getSimpleName();
    private static final String TABLE_TICKET_GRANTING_TICKETS = TicketGrantingTicketImpl.class.getSimpleName();

    private boolean lockTgt = true;

    @PersistenceContext(unitName = "ticketEntityManagerFactory")
    private EntityManager entityManager;

    public void setLockTgt(final boolean lockTgt) {
        this.lockTgt = lockTgt;
    }

    @Override
    public void updateTicket(final Ticket ticket) {
        this.entityManager.merge(ticket);
        logger.debug("Updated ticket [{}].", ticket);
    }

    @Override
    public void addTicket(final Ticket ticket) {
        this.entityManager.persist(ticket);
        logger.debug("Added ticket [{}] to registry.", ticket);
    }

    /**
     * Removes the ticket.
     *
     * @param ticket the ticket
     * @return true if ticket was removed
     */
    public boolean removeTicket(final Ticket ticket) {
        try {
            final ZonedDateTime creationDate = ticket.getCreationTime();
            logger.debug("Removing Ticket [{}] created: {}", ticket, creationDate.toString());
            this.entityManager.remove(ticket);
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
    public Ticket getRawTicket(final String ticketId) {
        try {
            if (ticketId.startsWith(TicketGrantingTicket.PREFIX)
                    || ticketId.startsWith(ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX)) {
                // There is no need to distinguish between TGTs and PGTs since PGTs inherit from TGTs
                return this.entityManager.find(TicketGrantingTicketImpl.class, ticketId,
                        this.lockTgt ? LockModeType.PESSIMISTIC_WRITE : null);
            }

            if (ticketId.startsWith(OAuthCode.PREFIX) || ticketId.startsWith(AccessToken.PREFIX)
                    || ticketId.startsWith(RefreshToken.PREFIX)) {
                return this.entityManager.find(OAuthCodeImpl.class, ticketId);
            }

            return this.entityManager.find(ServiceTicketImpl.class, ticketId);
        } catch (final Exception e) {
            logger.error("Error getting ticket {} from registry.", ticketId, e);
        }
        return null;
    }

    @Override
    public Collection<Ticket> getTickets() {
        final List<TicketGrantingTicketImpl> tgts = this.entityManager
                .createQuery("select t from " + TABLE_TICKET_GRANTING_TICKETS + " t",
                        TicketGrantingTicketImpl.class)
                .getResultList();
        final List<ServiceTicketImpl> sts = this.entityManager
                .createQuery("select s from " + TABLE_SERVICE_TICKETS + " s", ServiceTicketImpl.class)
                .getResultList();
        final List<OAuthCodeImpl> ots = this.entityManager
                .createQuery("select s from " + TABLE_OAUTH_TICKETS + " s", OAuthCodeImpl.class)
                .getResultList();

        final List<Ticket> tickets = new ArrayList<>(tgts);
        tickets.addAll(sts);
        tickets.addAll(ots);

        return tickets;
    }

    @Override
    public long sessionCount() {
        return countToLong(this.entityManager.createQuery(
                "select count(t) from " + TABLE_TICKET_GRANTING_TICKETS + " t").getSingleResult());
    }

    @Override
    public long serviceTicketCount() {
        return countToLong(this.entityManager.createQuery("select count(t) from "
                + TABLE_SERVICE_TICKETS + " t").getSingleResult());
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        final Ticket ticket = getTicket(ticketId);
        if (ticket == null) {
            return true;
        }

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

    /**
     * Gets ticket query result list.
     *
     * @param <T>      the type parameter
     * @param ticketId the ticket id
     * @param query    the query
     * @param clazz    the clazz
     * @return the ticket query result list
     */
    public <T extends Ticket> List<T> getTicketQueryResultList(final String ticketId, final String query,
                                                               final Class<T> clazz) {
        return this.entityManager.createQuery(query, clazz)
                .setParameter("id", ticketId)
                .getResultList();
    }

    /**
     * Delete o auth tokens int.
     *
     * @param ticketId the ticket id
     * @return the int
     */
    public int deleteOAuthTokens(final String ticketId) {
        final List<OAuthCodeImpl> oAuthCodeImpls = getTicketQueryResultList(ticketId,
                "select o from " + TABLE_OAUTH_TICKETS + " o where o.id = :id", OAuthCodeImpl.class);
        return deleteTicketsFromResultList(oAuthCodeImpls);
    }

    /**
     * Delete service tickets int.
     *
     * @param ticketId the ticket id
     * @return the int
     */
    public int deleteServiceTickets(final String ticketId) {
        final List<ServiceTicketImpl> serviceTicketImpls = getTicketQueryResultList(ticketId,
                "select s from " + TABLE_SERVICE_TICKETS + " s where s.id = :id", ServiceTicketImpl.class);
        return deleteTicketsFromResultList(serviceTicketImpls);
    }

    /**
     * Delete tickets from result list int.
     *
     * @param serviceTicketImpls the service ticket impls
     * @return the int
     */
    public int deleteTicketsFromResultList(final List<? extends Ticket> serviceTicketImpls) {
        int failureCount = 0;
        for (final Ticket serviceTicketImpl : serviceTicketImpls) {
            if (!removeTicket(serviceTicketImpl)) {
                failureCount++;
            }
        }
        return failureCount;
    }

    /**
     * Delete ticket granting tickets int.
     *
     * @param ticketId the ticket id
     * @return the int
     */
    public int deleteTicketGrantingTickets(final String ticketId) {
        int failureCount = 0;

        final List<ServiceTicketImpl> serviceTicketImpls = getTicketQueryResultList(ticketId,
                "select s from "
                        + TABLE_SERVICE_TICKETS
                        + " s where s.ticketGrantingTicket.id = :id", ServiceTicketImpl.class);
        failureCount += deleteTicketsFromResultList(serviceTicketImpls);

        List<TicketGrantingTicketImpl> ticketGrantingTicketImpls = getTicketQueryResultList(ticketId,
                "select t from " + TABLE_TICKET_GRANTING_TICKETS
                        + " t where t.ticketGrantingTicket.id = :id", TicketGrantingTicketImpl.class);
        failureCount += deleteTicketsFromResultList(ticketGrantingTicketImpls);

        ticketGrantingTicketImpls = getTicketQueryResultList(ticketId,
                "select t from " + TABLE_TICKET_GRANTING_TICKETS
                        + " t where t.id = :id", TicketGrantingTicketImpl.class);
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

}
