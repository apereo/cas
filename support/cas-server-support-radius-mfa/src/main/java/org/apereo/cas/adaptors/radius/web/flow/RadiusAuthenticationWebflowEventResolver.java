package org.apereo.cas.adaptors.radius.web.flow;

import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.authentication.BaseMultifactorAuthenticationProviderEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;

import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;
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

    public RadiusAuthenticationWebflowEventResolver(
        final CasWebflowEventResolutionConfigurationContext webflowEventResolutionConfigurationContext,
        final long allowedAuthenticationAttempts) {
        super(webflowEventResolutionConfigurationContext);
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
