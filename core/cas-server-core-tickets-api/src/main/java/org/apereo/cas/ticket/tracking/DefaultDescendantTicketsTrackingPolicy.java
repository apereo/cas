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
    public void trackTicket(final Ticket ownerTicket, final Ticket ticket) {
        if (ownerTicket instanceof final TicketGrantingTicket tgt) {
            tgt.getDescendantTickets().add(ticket.getId());
        }
    }
}
