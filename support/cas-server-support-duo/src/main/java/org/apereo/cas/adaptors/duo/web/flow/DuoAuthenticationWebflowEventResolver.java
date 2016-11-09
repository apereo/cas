package org.apereo.cas.adaptors.duo.web.flow;

import org.apereo.cas.web.flow.resolver.impl.AbstractCasWebflowEventResolver;
import org.apereo.inspektr.audit.annotation.Audit;
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

