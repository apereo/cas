package org.apereo.cas.web.flow.login;

import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link SessionStoreTicketGrantingTicketAction}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
public class SessionStoreTicketGrantingTicketAction extends AbstractAction {
    private final SessionStore<JEEContext> sessionStore;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(requestContext);
        val webContext = new JEEContext(request, response, sessionStore);
        sessionStore.set(webContext, WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID, ticketGrantingTicketId);
        return null;
    }
}
