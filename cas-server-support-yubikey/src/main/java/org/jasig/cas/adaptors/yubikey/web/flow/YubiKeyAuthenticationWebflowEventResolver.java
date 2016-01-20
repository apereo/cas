package org.jasig.cas.adaptors.yubikey.web.flow;

import org.jasig.cas.web.flow.authentication.AbstractCasWebflowEventResolver;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link YubiKeyAuthenticationWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("yubikeyAuthenticationWebflowEventResolver")
public class YubiKeyAuthenticationWebflowEventResolver extends AbstractCasWebflowEventResolver {
    @Override
    protected Set<Event> resolveInternal(final RequestContext context) {
        return handleAuthenticationTransactionAndGrantTicketGrantingTicket(context);
    }

}
