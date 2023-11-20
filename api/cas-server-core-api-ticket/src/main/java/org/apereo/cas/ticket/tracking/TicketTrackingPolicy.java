package org.apereo.cas.ticket.tracking;

import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;

/**
 * This is {@link TicketTrackingPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public interface TicketTrackingPolicy {
    /**
     * Tracking policy for service tickets.
     */
    String BEAN_NAME_SERVICE_TICKET_TRACKING = "serviceTicketSessionTrackingPolicy";

    /**
     * Tracking policy for descendant tickets.
     */
    String BEAN_NAME_DESCENDANT_TICKET_TRACKING = "descendantTicketsTrackingPolicy";

    /**
     * Track application attempt and access.
     * Typically, ticket-granting tickets keep track of applications
     * and service tickets for which they are authorized to issue tickets.
     *
     * @param ownerTicket the owner ticket
     * @param ticket      the tracked ticket
     */
    default void trackTicket(final TicketGrantingTicket ownerTicket, final Ticket ticket) {}

    /**
     * No op ticket tracking policy.
     *
     * @return the ticket tracking policy
     */
    static TicketTrackingPolicy noOp() {
        return new TicketTrackingPolicy() {};
    }
}
