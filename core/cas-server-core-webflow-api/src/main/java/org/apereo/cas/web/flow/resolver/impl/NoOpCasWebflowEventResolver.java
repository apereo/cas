package org.apereo.cas.web.flow.resolver.impl;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link NoOpCasWebflowEventResolver} that does not resolve any events.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class NoOpCasWebflowEventResolver extends AbstractCasWebflowEventResolver {

    public NoOpCasWebflowEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                       final CentralAuthenticationService centralAuthenticationService, final ServicesManager servicesManager,
                                       final TicketRegistrySupport ticketRegistrySupport, final CookieGenerator warnCookieGenerator,
                                       final AuthenticationServiceSelectionPlan authenticationSelectionStrategies,
                                       final MultifactorAuthenticationProviderSelector selector) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager, ticketRegistrySupport, warnCookieGenerator,
                authenticationSelectionStrategies, selector);
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        return null;
    }
}
