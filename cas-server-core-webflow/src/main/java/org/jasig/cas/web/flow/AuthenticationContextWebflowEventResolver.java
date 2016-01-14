package org.jasig.cas.web.flow;

import org.jasig.cas.authentication.AuthenticationResultBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AuthenticationContextWebflowEventResolver}
 * that decides the next event in the authentication web flow.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public interface AuthenticationContextWebflowEventResolver {

    /**
     * Resolve event.
     *
     * @param authenticationContextBuilder the authentication context builder
     * @param context               the context
     * @param messageContext        the message context
     * @return the event
     * @throws Exception the exception
     */
    Event resolve(final AuthenticationResultBuilder authenticationContextBuilder, RequestContext context, MessageContext messageContext)
            throws Exception;
}
