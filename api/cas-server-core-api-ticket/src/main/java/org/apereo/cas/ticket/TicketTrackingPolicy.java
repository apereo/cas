package org.apereo.cas.ticket;

/**
 * This is {@link TicketTrackingPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public interface TicketTrackingPolicy {
    /**
     * Default bean name.
     */
    String BEAN_NAME_SERVICE_TICKET_TRACKING = "serviceTicketSessionTrackingPolicy";

    /**
     * Track application attempt and access.
     * Typically, ticket-granting tickets keep track of applications
     * and service tickets for which they are authorized to issue tickets.
     *
     * @param ownerTicket the owner ticket
     * @param ticket      the tracked ticket
     */
    default void trackTicket(final TicketGrantingTicket ownerTicket, final Ticket ticket) {
    }
}
