package org.apereo.cas.ticket.tracking;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import lombok.val;

/**
 * This is {@link MostRecentProxyGrantingTicketTrackingPolicy}.
 *
 * @author Jerome LELEU
 * @since 8.0.0
 */
public class MostRecentProxyGrantingTicketTrackingPolicy implements TicketTrackingPolicy {
    
    /**
     * Static instance of the policy.
     */
    public static final TicketTrackingPolicy INSTANCE = new MostRecentProxyGrantingTicketTrackingPolicy();

    @Override
    public String trackTicket(final Ticket ownerTicket, final Ticket ticket, final Service service) {
        if (ownerTicket instanceof final TicketGrantingTicket ticketGrantingTicket) {
            val proxyGrantingTickets = ticketGrantingTicket.getProxyGrantingTickets();
            val serviceId = service.getId();
            proxyGrantingTickets.values().removeIf(existingService -> existingService.getId().equals(serviceId));
            proxyGrantingTickets.put(ticket.getId(), service);
        }
        return ticket.getId();
    }
}
