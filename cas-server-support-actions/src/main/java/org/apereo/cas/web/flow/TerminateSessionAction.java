package org.apereo.cas.web.flow;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.logout.LogoutRequest;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.DefaultAuthenticationSystemSupport;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Terminates the CAS SSO session by destroying all SSO state data (i.e. TGT, cookies).
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class TerminateSessionAction extends AbstractAction {

    /** Webflow event helper component. */
    private final EventFactorySupport eventFactorySupport = new EventFactorySupport();


    private CentralAuthenticationService centralAuthenticationService;
    
    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;

    private CookieRetrievingCookieGenerator warnCookieGenerator;
    
    private AuthenticationSystemSupport authenticationSystemSupport;

    /**
     * Creates a new instance with the given parameters.
     */
    public TerminateSessionAction() {}

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        return terminate(requestContext);
    }

    /**
     * Terminates the CAS SSO session by destroying the TGT (if any) and removing cookies related to the SSO session.
     *
     * @param context Request context.
     *
     * @return "success"
     */
    public Event terminate(final RequestContext context) {
        // in login's webflow : we can get the value from context as it has already been stored
        String tgtId = WebUtils.getTicketGrantingTicketId(context);
        // for logout, we need to get the cookie's value
        if (tgtId == null) {
            final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
            tgtId = this.ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        }
        if (tgtId != null) {
            final List<LogoutRequest> logoutRequests = this.centralAuthenticationService.destroyTicketGrantingTicket(tgtId);
            WebUtils.putLogoutRequests(context, logoutRequests);
        }
        final HttpServletResponse response = WebUtils.getHttpServletResponse(context);
        this.ticketGrantingTicketCookieGenerator.removeCookie(response);
        this.warnCookieGenerator.removeCookie(response);
        return this.eventFactorySupport.success(this);
    }

    public void setCentralAuthenticationService(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    public void setTicketGrantingTicketCookieGenerator(final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator) {
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
    }

    public void setWarnCookieGenerator(final CookieRetrievingCookieGenerator warnCookieGenerator) {
        this.warnCookieGenerator = warnCookieGenerator;
    }

    public void setAuthenticationSystemSupport(final AuthenticationSystemSupport authenticationSystemSupport) {
        this.authenticationSystemSupport = authenticationSystemSupport;
    }
}
