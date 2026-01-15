package org.apereo.cas.logout;

import module java.base;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;

/**
 * This is {@link DescendantTicketsLogoutPostProcessor}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class DescendantTicketsLogoutPostProcessor implements LogoutPostProcessor {
    private final TicketRegistry ticketRegistry;
    private final TicketTrackingPolicy descendantTicketsTrackingPolicy;

    @Override
    public void handle(final TicketGrantingTicket ticketGrantingTicket) {
        LOGGER.debug("CAS is configured to track and remove descendant tickets of the ticket-granting tickets");
        ticketGrantingTicket.getDescendantTickets().forEach(Unchecked.consumer(entry -> {
            LOGGER.trace("Deleting descendant ticket [{}] from the registry as a descendant of [{}]", entry, ticketGrantingTicket.getId());
            val ticket = descendantTicketsTrackingPolicy.extractTicket(entry);
            LOGGER.debug("Deleting ticket [{}] from the registry as a descendant of [{}]", ticket, ticketGrantingTicket.getId());
            ticketRegistry.deleteTicket(ticket);
        }));
    }
}
