package org.apereo.cas.ticket.tracking;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ServiceAwareTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apache.commons.lang3.StringUtils;

/**
 * This is the default tracking policy for descendant tickets.
 *
 * @author Jerome LELEU
 * @since 7.0.0
 */
public class DefaultDescendantTicketsTrackingPolicy implements TicketTrackingPolicy {
    private static final char DELIMITER = ',';

    @Override
    public String trackTicket(final Ticket ownerTicket, final Ticket ticket) {
        if (ownerTicket instanceof final TicketGrantingTicket tgt) {
            var trackedEntry = ticket.getId();
            if (ticket instanceof final ServiceAwareTicket sat) {
                trackedEntry += DELIMITER + sat.getService().getId();
            }
            tgt.getDescendantTickets().add(trackedEntry);
            return trackedEntry;
        }
        return TicketTrackingPolicy.super.trackTicket(ownerTicket, ticket);
    }

    @Override
    public String extractTicket(final String entry) {
        return StringUtils.substringBefore(entry, DELIMITER);
    }

    protected String extractService(final String entry) {
        return StringUtils.substringAfter(entry, DELIMITER);
    }

    @Override
    public long countTickets(final Ticket ticketGrantingTicket, final String ticketId) {
        if (ticketGrantingTicket instanceof final TicketGrantingTicket tgt) {
            return tgt.getDescendantTickets()
                .stream()
                .map(this::extractTicket)
                .filter(StringUtils::isNotBlank)
                .filter(id -> id.equals(ticketId))
                .count();
        }
        return TicketTrackingPolicy.super.countTickets(ticketGrantingTicket, ticketId);
    }

    @Override
    public long countTicketsFor(final Ticket ticketGrantingTicket, final Service service) {
        if (ticketGrantingTicket instanceof final TicketGrantingTicket tgt) {
            return tgt.getDescendantTickets()
                .stream()
                .map(this::extractService)
                .filter(StringUtils::isNotBlank)
                .filter(id -> id.equals(service.getId()))
                .count();
        }
        return TicketTrackingPolicy.super.countTicketsFor(ticketGrantingTicket, service);
    }
}
