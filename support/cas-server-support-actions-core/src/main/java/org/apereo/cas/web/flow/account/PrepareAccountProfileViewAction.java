package org.apereo.cas.web.flow.account;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Prepare the authenticated account for view.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
public class PrepareAccountProfileViewAction extends BaseCasWebflowAction {
    private final CentralAuthenticationService centralAuthenticationService;

    private final ServicesManager servicesManager;

    private final CasConfigurationProperties casProperties;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val tgt = WebUtils.getTicketGrantingTicketId(requestContext);
        val ticketGrantingTicket = FunctionUtils.doAndHandle(
            () -> Optional.of(centralAuthenticationService.getTicket(tgt, TicketGrantingTicket.class)),
            throwable -> Optional.<TicketGrantingTicket>empty()).get();

        ticketGrantingTicket.ifPresent(ticket -> {
            WebUtils.putAuthentication(ticket.getAuthentication(), requestContext);
            val service = WebUtils.getService(requestContext);
            if (casProperties.getView().isAuthorizedServicesOnSuccessfulLogin()) {
                val authzAttributes = (Map) CollectionUtils.merge(ticket.getAuthentication().getAttributes(),
                    ticket.getAuthentication().getPrincipal().getAttributes());
                val authorizedServices = servicesManager.getAllServices()
                    .stream()
                    .filter(registeredService -> FunctionUtils.doAndHandle(
                        () -> RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(service,
                            registeredService, ticket.getAuthentication().getPrincipal().getId(), authzAttributes),
                        throwable -> false).get())
                    .collect(Collectors.toList());
                WebUtils.putAuthorizedServices(requestContext, authorizedServices);
            }
        });

        return success();
    }
}
