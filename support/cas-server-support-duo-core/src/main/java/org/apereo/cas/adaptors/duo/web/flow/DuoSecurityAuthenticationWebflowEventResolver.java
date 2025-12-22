package org.apereo.cas.adaptors.duo.web.flow;

import module java.base;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.web.flow.authentication.BaseMultifactorAuthenticationProviderEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.inspektr.audit.annotation.Audit;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DuoSecurityAuthenticationWebflowEventResolver }.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DuoSecurityAuthenticationWebflowEventResolver extends BaseMultifactorAuthenticationProviderEventResolver {

    public DuoSecurityAuthenticationWebflowEventResolver(
        final CasWebflowEventResolutionConfigurationContext webflowEventResolutionConfigurationContext) {
        super(webflowEventResolutionConfigurationContext);
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext requestContext) throws Throwable {
        return handleAuthenticationTransactionAndGrantTicketGrantingTicket(requestContext);
    }

    @Audit(action = AuditableActions.AUTHENTICATION_EVENT,
        actionResolverName = AuditActionResolvers.AUTHENTICATION_EVENT_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.AUTHENTICATION_EVENT_RESOURCE_RESOLVER)
    @Override
    public @Nullable Event resolveSingle(final RequestContext context) throws Throwable {
        return super.resolveSingle(context);
    }
}

