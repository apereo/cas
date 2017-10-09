package org.apereo.cas.web.flow;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Action that handles the TicketGrantingTicket creation and destruction. If the
 * action is given a TicketGrantingTicket and one also already exists, the old
 * one is destroyed and replaced with the new one. This action always returns
 * "success".
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class SendTicketGrantingTicketAction extends AbstractAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendTicketGrantingTicketAction.class);

    private final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;
    private final CentralAuthenticationService centralAuthenticationService;
    private final SingleSignOnParticipationStrategy renewalStrategy;

    public SendTicketGrantingTicketAction(final CentralAuthenticationService centralAuthenticationService,
                                          final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator,
                                          final SingleSignOnParticipationStrategy renewalStrategy) {
        this.centralAuthenticationService = centralAuthenticationService;
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
        this.renewalStrategy = renewalStrategy;
    }

    @Override
    protected Event doExecute(final RequestContext context) {
        final String ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(context);
        final String ticketGrantingTicketValueFromCookie = (String) context.getFlowScope().get(WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID);

        if (StringUtils.isBlank(ticketGrantingTicketId)) {
            LOGGER.debug("No ticket-granting ticket is found in the context.");
            return success();
        }

        if (WebUtils.isAuthenticatingAtPublicWorkstation(context)) {
            LOGGER.info("Authentication is at a public workstation. SSO cookie will not be generated. Requests will be challenged for authentication.");
        } else if (this.renewalStrategy.isParticipating(context)) {
            LOGGER.debug("Setting ticket-granting cookie for current session linked to [{}].", ticketGrantingTicketId);
            this.ticketGrantingTicketCookieGenerator.addCookie(context, ticketGrantingTicketId);
        } else {
            LOGGER.info("Authentication session is renewed but CAS is not configured to create the SSO session. "
                    + "SSO cookie will not be generated. Subsequent requests will be challenged for credentials.");
        }

        if (ticketGrantingTicketValueFromCookie != null && !ticketGrantingTicketId.equals(ticketGrantingTicketValueFromCookie)) {
            LOGGER.debug("Ticket-granting ticket from TGC does not match the ticket-granting ticket from context");
            this.centralAuthenticationService.destroyTicketGrantingTicket(ticketGrantingTicketValueFromCookie);
        }

        return success();
    }
}
