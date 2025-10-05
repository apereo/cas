package org.apereo.cas.ticket.tracking;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import lombok.val;

/**
 * This is {@link MostRecentProxyGrantingTicketTrackingPolicy}.
 *
 * @author Jerome LELEU
 * @since 7.4.0
 */
public class MostRecentProxyGrantingTicketTrackingPolicy extends AllProxyGrantingTicketsTrackingPolicy {
    
    /**
     * Static instance of the policy.
     */
    public static final TicketTrackingPolicy INSTANCE = new MostRecentProxyGrantingTicketTrackingPolicy();

    @Override
    public String trackTicket(final Ticket ownerTicket, final Ticket ticket, final Object... parameters) {
        if (ownerTicket instanceof TicketGrantingTicket ticketGrantingTicket) {
            val proxyGrantingTickets = ticketGrantingTicket.getProxyGrantingTickets();
            val service = (Service) parameters[0];
            val serviceId = service.getId();
            for (val proxyGrantingTicket : proxyGrantingTickets.entrySet()) {
                val existingService = proxyGrantingTicket.getValue();
                if (existingService.getId().equals(serviceId)) {
                    val pgtId = proxyGrantingTicket.getKey();
                    proxyGrantingTickets.remove(pgtId);
                    break;
                }
            }
        }
        return super.trackTicket(ownerTicket, ticket, parameters);
    }
}
