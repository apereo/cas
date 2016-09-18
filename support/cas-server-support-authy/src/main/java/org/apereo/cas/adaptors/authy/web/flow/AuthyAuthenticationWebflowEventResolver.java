package org.apereo.cas.adaptors.authy.web.flow;

import org.apereo.cas.web.flow.resolver.impl.AbstractCasWebflowEventResolver;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link AuthyAuthenticationWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class AuthyAuthenticationWebflowEventResolver extends AbstractCasWebflowEventResolver {
    @Override
    protected Set<Event> resolveInternal(final RequestContext context) {
        return handleAuthenticationTransactionAndGrantTicketGrantingTicket(context);
    }

}
