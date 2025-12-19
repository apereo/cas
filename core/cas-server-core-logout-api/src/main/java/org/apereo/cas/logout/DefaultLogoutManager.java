package org.apereo.cas.logout;

import module java.base;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.logout.slo.SingleLogoutExecutionRequest;
import org.apereo.cas.logout.slo.SingleLogoutRequestContext;
import org.apereo.cas.logout.slo.SingleLogoutServiceMessageHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.inspektr.audit.annotation.Audit;

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

    private static <T> Predicate<T> distinctByKey(final Function<? super T, Object> keyExtractor) {
        val seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    @Override
    @Audit(
        action = AuditableActions.LOGOUT,
        actionResolverName = AuditActionResolvers.LOGOUT_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.LOGOUT_RESOURCE_RESOLVER)
    public List<SingleLogoutRequestContext> performLogout(final SingleLogoutExecutionRequest context) {
        val ticket = context.getTicketGrantingTicket();
        LOGGER.info("Performing logout operations for [{}]", ticket.getId());
        if (this.singleLogoutCallbacksDisabled) {
            LOGGER.info("Single logout callbacks are disabled");
            return new ArrayList<>();
        }
        val logoutRequests = performLogoutForTicket(context);
        logoutExecutionPlan.getLogoutPostProcessors().forEach(postProcessor -> {
            LOGGER.debug("Invoking logout handler [{}] to process ticket [{}]", postProcessor.getClass().getSimpleName(), ticket.getId());
            postProcessor.handle(ticket);
        });
        LOGGER.info("[{}] logout requests were processed", logoutRequests.size());
        return logoutRequests;
    }

    private List<SingleLogoutRequestContext> performLogoutForTicket(final SingleLogoutExecutionRequest context) {
        val ticketToBeLoggedOut = context.getTicketGrantingTicket();
        val streamServices = new LinkedHashMap<String, Service>();
        val services = ticketToBeLoggedOut.getServices();
        streamServices.putAll(services);
        streamServices.putAll(ticketToBeLoggedOut.getProxyGrantingTickets());
        val logoutServices = streamServices
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue() instanceof WebApplicationService)
            .map(entry -> Pair.of(entry.getKey(), (WebApplicationService) entry.getValue())).toList();

        val sloHandlers = logoutExecutionPlan.getSingleLogoutServiceMessageHandlers();
        return logoutServices
            .stream()
            .map(entry -> sloHandlers
                .stream()
                .sorted(Comparator.comparing(SingleLogoutServiceMessageHandler::getOrder))
                .filter(handler -> handler.supports(context, entry.getValue()))
                .map(handler -> {
                    val service = entry.getValue();
                    LOGGER.trace("Handling single logout callback for [{}]", service.getId());
                    return handler.handle(service, entry.getKey(), context);
                })
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()))
            .flatMap(Collection::stream)
            .filter(distinctByKey(SingleLogoutRequestContext::getService))
            .collect(Collectors.toList());
    }
}
