package org.apereo.cas.ticket.tracking;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;

/**
 * This is {@link MostRecentServiceSessionTrackingPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class MostRecentServiceSessionTrackingPolicy extends AllServicesSessionTrackingPolicy {

    public MostRecentServiceSessionTrackingPolicy(final TicketRegistry ticketRegistry) {
        super(ticketRegistry);
    }

    @Override
    protected void beforeTrackingServiceTicket(final Ticket ownerTicket,
                                               final ServiceTicket serviceTicket) {
        val ticketGrantingTicket = (TicketGrantingTicket) ownerTicket;
        val path = normalizePath(serviceTicket.getService());
        val toRemove = ticketGrantingTicket.getServices()
            .entrySet()
            .stream()
            .filter(entry -> {
                val normalizedExistingPath = normalizePath(entry.getValue());
                return path.equals(normalizedExistingPath);
            }).toList();

        toRemove.forEach(Unchecked.consumer(entry -> {
            ticketGrantingTicket.getServices().remove(entry.getKey());
            ticketRegistry.deleteTicket(entry.getKey());
        }));
    }

    /**
     * Normalize the path of a service by removing the query string and everything after a semi-colon.
     *
     * @param service the service to normalize
     * @return the normalized path
     */
    private static String normalizePath(final Service service) {
        var path = service.getId();
        path = StringUtils.substringBefore(path, "?");
        path = StringUtils.substringBefore(path, ";");
        path = StringUtils.substringBefore(path, "#");
        return path;
    }
}
