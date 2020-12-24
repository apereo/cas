package org.apereo.cas.web.flow.logout;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Confirm logout action once the confirmation view is displayed.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiredArgsConstructor
public class ConfirmLogoutAction extends AbstractAction {

    /**
     * The cas service.
     */
    protected final CentralAuthenticationService centralAuthenticationService;

    /**
     * The TGT cookie generator.
     */
    protected final CasCookieBuilder ticketGrantingTicketCookieGenerator;

    @Override
    public Event doExecute(final RequestContext requestContext) {
        val tgtId = getTicketGrantingTicket(requestContext);
        if (StringUtils.isNotBlank(tgtId)) {
            val ticket = centralAuthenticationService.getTicket(tgtId, TicketGrantingTicket.class);
            WebUtils.putAuthentication(ticket.getAuthentication(), requestContext);
            WebUtils.putTicketGrantingTicket(requestContext, ticket);
        }
        return success();
    }

    private String getTicketGrantingTicket(final RequestContext context) {
        val tgtId = WebUtils.getTicketGrantingTicketId(context);
        if (StringUtils.isBlank(tgtId)) {
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
            return this.ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        }
        return tgtId;
    }
}
