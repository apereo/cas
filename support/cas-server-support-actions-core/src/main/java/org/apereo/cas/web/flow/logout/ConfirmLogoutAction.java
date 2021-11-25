package org.apereo.cas.web.flow.logout;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutExecutionPlan;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Confirm logout action once the confirmation view is displayed.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class ConfirmLogoutAction extends AbstractLogoutAction {

    public ConfirmLogoutAction(final CentralAuthenticationService centralAuthenticationService,
                               final CasCookieBuilder ticketGrantingTicketCookieGenerator,
                               final ArgumentExtractor argumentExtractor,
                               final ServicesManager servicesManager,
                               final LogoutExecutionPlan logoutExecutionPlan,
                               final CasConfigurationProperties casProperties) {
        super(centralAuthenticationService, ticketGrantingTicketCookieGenerator,
            argumentExtractor, servicesManager, logoutExecutionPlan, casProperties);
    }

    @Override
    protected Event doInternalExecute(final HttpServletRequest request, final HttpServletResponse response,
                                      final RequestContext requestContext) {
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
