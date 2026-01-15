package org.apereo.cas.web.flow.login;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.RegisteredServicePrincipalAccessStrategyEnforcer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Action that should execute prior to rendering the generic-success login view.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class GenericSuccessViewAction extends BaseCasWebflowAction {
    private final TicketRegistry ticketRegistry;

    private final ServicesManager servicesManager;

    private final ServiceFactory<WebApplicationService> serviceFactory;

    private final CasConfigurationProperties casProperties;

    private final RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer;
    
    private Optional<Authentication> getAuthentication(final String ticketGrantingTicketId) {
        try {
            val ticketGrantingTicket = ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
            return Optional.of(ticketGrantingTicket.getAuthentication());
        } catch (final InvalidTicketException e) {
            LOGGER.debug(e.getMessage(), e);
        }
        LOGGER.warn("In the absence of valid ticket-granting ticket, the authentication cannot be determined");
        return Optional.empty();
    }

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        val redirectUrl = casProperties.getView().getDefaultRedirectUrl();
        if (StringUtils.isNotBlank(redirectUrl)) {
            val service = this.serviceFactory.createService(redirectUrl);
            val registeredService = this.servicesManager.findServiceBy(service);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
            requestContext.getExternalContext().requestExternalRedirect(service.getId());
        } else {
            val tgt = WebUtils.getTicketGrantingTicketId(requestContext);
            getAuthentication(tgt).ifPresent(authn -> {
                WebUtils.putAuthentication(authn, requestContext);
                val service = WebUtils.getService(requestContext);
                if (casProperties.getView().isAuthorizedServicesOnSuccessfulLogin()) {
                    val authorizedServices = servicesManager.getAllServices()
                        .stream()
                        .filter(registeredService -> {
                            try {
                                return principalAccessStrategyEnforcer.authorize(
                                    RegisteredServicePrincipalAccessStrategyEnforcer.PrincipalAccessStrategyContext.builder()
                                        .registeredService(registeredService)
                                        .principalId(authn.getPrincipal().getId())
                                        .principalAttributes(CollectionUtils.merge(authn.getAttributes(), authn.getPrincipal().getAttributes()))
                                        .service(service)
                                        .applicationContext(requestContext.getActiveFlow().getApplicationContext())
                                        .build());
                            } catch (final Throwable e) {
                                LOGGER.info(e.getMessage(), e);
                                return false;
                            }
                        })
                        .collect(Collectors.toList());
                    WebUtils.putAuthorizedServices(requestContext, authorizedServices);
                }
            });
        }
        return success();
    }
}
