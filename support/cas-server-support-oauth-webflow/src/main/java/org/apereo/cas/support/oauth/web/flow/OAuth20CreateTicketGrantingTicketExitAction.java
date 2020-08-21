package org.apereo.cas.support.oauth.web.flow;

import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link OAuth20CreateTicketGrantingTicketExitAction}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
public class OAuth20CreateTicketGrantingTicketExitAction extends AbstractAction {
    private final SessionStore<JEEContext> oauthDistributedSessionStore;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(requestContext);
        val webContext = new JEEContext(request, response, oauthDistributedSessionStore);
        oauthDistributedSessionStore.set(webContext, WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID, ticketGrantingTicketId);
        return null;
    }
}
