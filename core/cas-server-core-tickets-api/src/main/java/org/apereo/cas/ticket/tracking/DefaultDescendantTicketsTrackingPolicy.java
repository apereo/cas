package org.apereo.cas.ticket.tracking;

import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;

/**
 * This is the default tracking policy for descendant tickets.
 *
 * @author Jerome LELEU
 * @since 7.0.0
 */
public class DefaultDescendantTicketsTrackingPolicy implements TicketTrackingPolicy {
    @Override
    public void trackTicket(final TicketGrantingTicket ownerTicket, final Ticket ticket) {
        if (ownerTicket != null) {
            ownerTicket.getDescendantTickets().add(ticket.getId());
        }
    }
}
