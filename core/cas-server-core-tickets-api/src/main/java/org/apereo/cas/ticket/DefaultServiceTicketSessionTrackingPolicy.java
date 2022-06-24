package org.apereo.cas.ticket;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.registry.TicketRegistry;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;

import java.util.stream.Collectors;

/**
 * This is {@link DefaultServiceTicketSessionTrackingPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
public class DefaultServiceTicketSessionTrackingPolicy implements ServiceTicketSessionTrackingPolicy {
    private final CasConfigurationProperties casProperties;

    private final TicketRegistry ticketRegistry;

    @Override
    public synchronized void track(final AuthenticatedServicesAwareTicketGrantingTicket ownerTicket,
                                   final ServiceTicket serviceTicket) {
        ownerTicket.update();
        serviceTicket.getService().setPrincipal(ownerTicket.getRoot().getAuthentication().getPrincipal().getId());

        val onlyTrackMostRecentSession = casProperties.getTicket().getTgt().getCore().isOnlyTrackMostRecentSession();
        if (onlyTrackMostRecentSession) {
            val path = normalizePath(serviceTicket.getService());
            val toRemove = ownerTicket.getServices()
                .entrySet()
                .stream()
                .filter(entry -> {
                    val normalizedExistingPath = normalizePath(entry.getValue());
                    return path.equals(normalizedExistingPath);
                })
                .collect(Collectors.toList());

            toRemove.forEach(Unchecked.consumer(entry -> {
                ownerTicket.getServices().remove(entry.getKey());
                ticketRegistry.deleteTicket(entry.getKey());
            }));
        }
        ownerTicket.getServices().put(serviceTicket.getId(), serviceTicket.getService());
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
