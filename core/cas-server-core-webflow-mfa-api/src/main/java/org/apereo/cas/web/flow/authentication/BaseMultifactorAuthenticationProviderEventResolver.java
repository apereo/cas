package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.resolver.impl.AbstractCasMultifactorAuthenticationWebflowEventResolver;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link BaseMultifactorAuthenticationProviderEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public abstract class BaseMultifactorAuthenticationProviderEventResolver extends AbstractCasMultifactorAuthenticationWebflowEventResolver {

    public BaseMultifactorAuthenticationProviderEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                              final CentralAuthenticationService centralAuthenticationService,
                                                              final ServicesManager servicesManager,
                                                              final TicketRegistrySupport ticketRegistrySupport,
                                                              final CookieGenerator warnCookieGenerator,
                                                              final AuthenticationServiceSelectionPlan authenticationSelectionStrategies,
                                                              final MultifactorAuthenticationProviderSelector selector,
                                                              final ApplicationEventPublisher eventPublisher,
                                                              final ConfigurableApplicationContext applicationContext) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager,
            ticketRegistrySupport, warnCookieGenerator,
            authenticationSelectionStrategies, selector, eventPublisher, applicationContext);
    }


    /**
     * Resolve registered service in request context.
     *
     * @param requestContext the request context
     * @return the registered service
     */
    protected RegisteredService resolveRegisteredServiceInRequestContext(final RequestContext requestContext) {
        val resolvedService = resolveServiceFromAuthenticationRequest(requestContext);
        if (resolvedService != null) {
            val service = this.servicesManager.findServiceBy(resolvedService);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(resolvedService, service);
            return service;
        }
        LOGGER.debug("Authentication request is not accompanied by a service given none is specified");
        return null;
    }
}
