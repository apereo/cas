package org.apereo.cas.adaptors.duo.web.flow;

import org.apereo.cas.web.flow.resolver.AbstractCasWebflowEventResolver;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link DuoAuthenticationWebflowEventResolver }.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Component("duoAuthenticationWebflowEventResolver")
public class DuoAuthenticationWebflowEventResolver extends AbstractCasWebflowEventResolver {

    @Override
    protected Set<Event> resolveInternal(final RequestContext requestContext) {
        return handleAuthenticationTransactionAndGrantTicketGrantingTicket(requestContext);
    }
}

