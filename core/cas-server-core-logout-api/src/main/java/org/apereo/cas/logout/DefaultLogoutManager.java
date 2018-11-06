package org.apereo.cas.logout;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.logout.slo.SingleLogoutRequest;
import org.apereo.cas.ticket.TicketGrantingTicket;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This logout manager handles the Single Log Out process.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class DefaultLogoutManager implements LogoutManager {
    private final boolean singleLogoutCallbacksDisabled;
    private final LogoutExecutionPlan logoutExecutionPlan;

    @Override
    public List<SingleLogoutRequest> performLogout(final TicketGrantingTicket ticket) {
        LOGGER.info("Performing logout operations for [{}]", ticket.getId());
        if (this.singleLogoutCallbacksDisabled) {
            LOGGER.info("Single logout callbacks are disabled");
            return new ArrayList<>(0);
        }
        val logoutRequests = performLogoutForTicket(ticket);
        this.logoutExecutionPlan.getLogoutPostProcessor().forEach(h -> {
            LOGGER.debug("Invoking logout handler [{}] to process ticket [{}]", h.getClass().getSimpleName(), ticket.getId());
            h.handle(ticket);
        });
        LOGGER.info("[{}] logout requests were processed", logoutRequests.size());
        return logoutRequests;
    }

    private List<SingleLogoutRequest> performLogoutForTicket(final TicketGrantingTicket ticketToBeLoggedOut) {
        val streamServices = Stream.concat(Stream.of(ticketToBeLoggedOut.getServices()), Stream.of(ticketToBeLoggedOut.getProxyGrantingTickets()));
        val logoutServices = streamServices
            .map(Map::entrySet)
            .flatMap(Set::stream)
            .filter(entry -> entry.getValue() instanceof WebApplicationService)
            .filter(Objects::nonNull)
            .map(entry -> Pair.of(entry.getKey(), (WebApplicationService) entry.getValue()))
            .collect(Collectors.toList());

        val sloHandlers = logoutExecutionPlan.getSingleLogoutServiceMessageHandlers();
        return logoutServices.stream()
            .map(entry -> sloHandlers
                .stream()
                .filter(handler -> handler.supports(entry.getValue()))
                .map(handler -> {
                    val service = entry.getValue();
                    LOGGER.trace("Handling single logout callback for [{}]", service.getId());
                    return handler.handle(service, entry.getKey(), ticketToBeLoggedOut);
                })
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }
}
