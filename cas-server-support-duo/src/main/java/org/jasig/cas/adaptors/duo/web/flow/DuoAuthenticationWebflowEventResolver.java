package org.jasig.cas.adaptors.duo.web.flow;

import org.jasig.cas.web.flow.authentication.AbstractCasWebflowEventResolver;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link DuoAuthenticationWebflowEventResolver }.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("duoAuthenticationWebflowEventResolver")
public class DuoAuthenticationWebflowEventResolver extends AbstractCasWebflowEventResolver {

    @Override
    protected Set<Event> resolveInternal(final RequestContext requestContext) {
        return handleAuthenticationTransactionAndGrantTicketGrantingTicket(requestContext);
    }
}

