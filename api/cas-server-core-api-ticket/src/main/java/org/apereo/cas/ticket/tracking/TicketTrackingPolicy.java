package org.apereo.cas.ticket.tracking;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.Ticket;

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
    default String trackTicket(final Ticket ownerTicket, final Ticket ticket) {
        return null;
    }

    /**
     * Count tickets for a given service.
     *
     * @param ticketGrantingTicket the ticket granting ticket
     * @param service              the service
     * @return the count
     */
    default long countTicketsFor(final Ticket ticketGrantingTicket, final Service service) {
        return 0;
    }

    /**
     * Extract ticket string.
     *
     * @param entry the entry
     * @return the string
     */
    default String extractTicket(final String entry) {
        return null;
    }

    /**
     * Count tickets.
     *
     * @param ticketGrantingTicket the ticket granting ticket
     * @param ticketId             the ticket id
     * @return the long
     */
    default long countTickets(final Ticket ticketGrantingTicket, final String ticketId) {
        return 0;
    }

    /**
     * No op ticket tracking policy.
     *
     * @return the ticket tracking policy
     */
    static TicketTrackingPolicy noOp() {
        return new TicketTrackingPolicy() {
        };
    }
}
