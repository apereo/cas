package org.apereo.cas.adaptors.authy.web.flow;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.web.flow.authentication.BaseMultifactorAuthenticationProviderEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;

import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link AuthyAuthenticationWebflowEventResolver}.
 *
 * @author Jérémie POISSON
 * 
 */
public class AuthyAuthenticationWebflowEventResolver extends BaseMultifactorAuthenticationProviderEventResolver {

    public AuthyAuthenticationWebflowEventResolver(final CasWebflowEventResolutionConfigurationContext webflowEventResolutionConfigurationContext) {
        super(webflowEventResolutionConfigurationContext);
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        return handleAuthenticationTransactionAndGrantTicketGrantingTicket(context);
    }

    @Audit(action = AuditableActions.AUTHENTICATION_EVENT,
        actionResolverName = AuditActionResolvers.AUTHENTICATION_EVENT_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.AUTHENTICATION_EVENT_RESOURCE_RESOLVER)
    @Override
    public Event resolveSingle(final RequestContext context) {
        return super.resolveSingle(context);
    }
}
