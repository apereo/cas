package org.apereo.cas.adaptors.radius.web.flow;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.authentication.BaseMultifactorAuthenticationProviderEventResolver;

import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link RadiusAuthenticationWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class RadiusAuthenticationWebflowEventResolver extends BaseMultifactorAuthenticationProviderEventResolver {
    private static final String FLOW_SCOPE_ATTR_TOTAL_AUTHENTICATION_ATTEMPTS = "totalAuthenticationAttempts";
    private final long allowedAuthenticationAttempts;

    public RadiusAuthenticationWebflowEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                    final CentralAuthenticationService centralAuthenticationService,
                                                    final ServicesManager servicesManager,
                                                    final TicketRegistrySupport ticketRegistrySupport,
                                                    final CookieGenerator warnCookieGenerator,
                                                    final AuthenticationServiceSelectionPlan authenticationSelectionStrategies,
                                                    final MultifactorAuthenticationProviderSelector selector,
                                                    final long allowedAuthenticationAttempts,
                                                    final ApplicationEventPublisher eventPublisher,
                                                    final ConfigurableApplicationContext applicationContext) {
        super(authenticationSystemSupport, centralAuthenticationService,
            servicesManager, ticketRegistrySupport, warnCookieGenerator,
            authenticationSelectionStrategies, selector, eventPublisher, applicationContext);
        this.allowedAuthenticationAttempts = allowedAuthenticationAttempts;
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        return handleAuthenticationTransactionAndGrantTicketGrantingTicket(context);
    }

    @Audit(action = "AUTHENTICATION_EVENT",
        actionResolverName = "AUTHENTICATION_EVENT_ACTION_RESOLVER",
        resourceResolverName = "AUTHENTICATION_EVENT_RESOURCE_RESOLVER")
    @Override
    public Event resolveSingle(final RequestContext context) {
        return super.resolveSingle(context);
    }

    @Override
    protected Event getAuthenticationFailureErrorEvent(final RequestContext context) {
        if (allowedAuthenticationAttempts <= 0) {
            return super.getAuthenticationFailureErrorEvent(context);
        }
        val attempts = context.getFlowScope().getLong(FLOW_SCOPE_ATTR_TOTAL_AUTHENTICATION_ATTEMPTS, 0L) + 1;
        if (attempts >= allowedAuthenticationAttempts) {
            context.getFlowScope().remove(FLOW_SCOPE_ATTR_TOTAL_AUTHENTICATION_ATTEMPTS);
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_CANCEL);
        }
        context.getFlowScope().put(FLOW_SCOPE_ATTR_TOTAL_AUTHENTICATION_ATTEMPTS, attempts + 1);
        return super.getAuthenticationFailureErrorEvent(context);
    }

}
