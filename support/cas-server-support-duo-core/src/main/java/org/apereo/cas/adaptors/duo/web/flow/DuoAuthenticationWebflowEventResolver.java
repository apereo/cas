package org.apereo.cas.adaptors.duo.web.flow;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.resolver.impl.AbstractCasWebflowEventResolver;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link DuoAuthenticationWebflowEventResolver }.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DuoAuthenticationWebflowEventResolver extends AbstractCasWebflowEventResolver {

    public DuoAuthenticationWebflowEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                 final CentralAuthenticationService centralAuthenticationService, final ServicesManager servicesManager,
                                                 final TicketRegistrySupport ticketRegistrySupport, final CookieGenerator warnCookieGenerator,
                                                 final AuthenticationServiceSelectionPlan authenticationSelectionStrategies,
                                                 final MultifactorAuthenticationProviderSelector selector) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager, ticketRegistrySupport, warnCookieGenerator,
                authenticationSelectionStrategies, selector);
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext requestContext) {
        return handleAuthenticationTransactionAndGrantTicketGrantingTicket(requestContext);
    }

    @Audit(action = "AUTHENTICATION_EVENT", actionResolverName = "AUTHENTICATION_EVENT_ACTION_RESOLVER",
            resourceResolverName = "AUTHENTICATION_EVENT_RESOURCE_RESOLVER")
    @Override
    public Event resolveSingle(final RequestContext context) {
        return super.resolveSingle(context);
    }
}

