package org.apereo.cas.ticket.tracking;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;

/**
 * This is {@link AllProxyGrantingTicketsTrackingPolicy}.
 *
 * @author Jerome LELEU
 * @since 7.4.0
 */
public class AllProxyGrantingTicketsTrackingPolicy implements TicketTrackingPolicy {
    
    /**
     * Static instance of the policy.
     */
    public static final TicketTrackingPolicy INSTANCE = new AllProxyGrantingTicketsTrackingPolicy();

    @Override
    public String trackTicket(final Ticket ownerTicket, final Ticket ticket, final Object... parameters) {
        if (ownerTicket instanceof TicketGrantingTicket ticketGrantingTicket) {
            ticketGrantingTicket.getProxyGrantingTickets().put(ticket.getId(), (Service) parameters[0]);
        }
        return null;
    }
}
