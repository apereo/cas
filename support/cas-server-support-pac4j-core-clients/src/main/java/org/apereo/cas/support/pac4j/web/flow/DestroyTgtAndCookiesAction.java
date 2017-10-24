package org.apereo.cas.support.pac4j.web.flow;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.model.core.logout.LogoutProperties;
import org.apereo.cas.logout.LogoutRequest;
import org.apereo.cas.web.flow.TerminateSessionAction;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import com.google.common.base.Throwables;


/**
 * A light version of {@link TerminateSessionAction} that does NOT destroy the HTTP session, only destroys the TGT and cookies.
 * 
 * It is assumed that the session itself will be terminated in {@link TerminateSessionFlowExecutionListener}.
 * 
 * @see TerminateSessionFlowExecutionListener
 * 
 * @author jkacer
 */
public class DestroyTgtAndCookiesAction extends TerminateSessionAction {

    private final Logger logger2 = LoggerFactory.getLogger(DestroyTgtAndCookiesAction.class);

    private final EventFactorySupport eventFactorySupport;
    private final CentralAuthenticationService centralAuthenticationService;
    private final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;
    private final CookieRetrievingCookieGenerator warnCookieGenerator;


    public DestroyTgtAndCookiesAction(
            final CentralAuthenticationService centralAuthenticationService,
            final CookieRetrievingCookieGenerator tgtCookieGenerator,
            final CookieRetrievingCookieGenerator warnCookieGenerator,
            final LogoutProperties logoutProperties) {
        super(centralAuthenticationService, tgtCookieGenerator, warnCookieGenerator, logoutProperties);
        this.eventFactorySupport = new EventFactorySupport();
        this.centralAuthenticationService = centralAuthenticationService;
        this.ticketGrantingTicketCookieGenerator = tgtCookieGenerator;
        this.warnCookieGenerator = warnCookieGenerator;
    }


    @Override
    public Event terminate(final RequestContext context) {
        // in login's webflow : we can get the value from context as it has already been stored
        try {
            final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
            final HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);

            String tgtId = WebUtils.getTicketGrantingTicketId(context);
            // for logout, we need to get the cookie's value
            if (tgtId == null) {
                tgtId = this.ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
            }
            if (tgtId != null) {
                logger2.debug("Destroying SSO session linked to ticket-granting ticket [{}]", tgtId);
                final List<LogoutRequest> logoutRequests = this.centralAuthenticationService.destroyTicketGrantingTicket(tgtId);
                WebUtils.putLogoutRequests(context, logoutRequests);
            }
            logger2.debug("Removing CAS cookies");
            this.ticketGrantingTicketCookieGenerator.removeCookie(response);
            this.warnCookieGenerator.removeCookie(response);

            // Do NOT destroy the session here. Keep it.
            return this.eventFactorySupport.success(this);
        } catch (final Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

}
