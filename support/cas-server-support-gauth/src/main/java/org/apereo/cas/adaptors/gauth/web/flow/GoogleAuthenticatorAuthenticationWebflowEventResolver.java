package org.apereo.cas.adaptors.gauth.web.flow;

import org.apereo.cas.web.flow.resolver.impl.AbstractCasWebflowEventResolver;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link GoogleAuthenticatorAuthenticationWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GoogleAuthenticatorAuthenticationWebflowEventResolver extends AbstractCasWebflowEventResolver {
    @Override
    protected Set<Event> resolveInternal(final RequestContext context) {
        return handleAuthenticationTransactionAndGrantTicketGrantingTicket(context);
    }

}
