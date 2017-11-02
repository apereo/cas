package org.apereo.cas.web.flow;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.model.core.logout.LogoutProperties;
import org.apereo.cas.logout.LogoutRequest;
import org.apereo.cas.util.Pac4jUtils;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.profile.ProfileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Terminates the CAS SSO session by destroying all SSO state data (i.e. TGT, cookies).
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class TerminateSessionAction extends AbstractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(TerminateSessionAction.class);

    private final EventFactorySupport eventFactorySupport = new EventFactorySupport();
    private final CentralAuthenticationService centralAuthenticationService;
    private final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;
    private final CookieRetrievingCookieGenerator warnCookieGenerator;
    private final LogoutProperties logoutProperties;

    public TerminateSessionAction(final CentralAuthenticationService centralAuthenticationService,
                                  final CookieRetrievingCookieGenerator tgtCookieGenerator,
                                  final CookieRetrievingCookieGenerator warnCookieGenerator,
                                  final LogoutProperties logoutProperties) {
        this.centralAuthenticationService = centralAuthenticationService;
        this.ticketGrantingTicketCookieGenerator = tgtCookieGenerator;
        this.warnCookieGenerator = warnCookieGenerator;
        this.logoutProperties = logoutProperties;
    }

    @Override
    public Event doExecute(final RequestContext requestContext) {
        boolean terminateSession = true;
        if (logoutProperties.isConfirmLogout()) {
            terminateSession = isLogoutRequestConfirmed(requestContext);
        }
        if (terminateSession) {
            return terminate(requestContext);
        }
        return this.eventFactorySupport.event(this, CasWebflowConstants.STATE_ID_WARN);
    }

    /**
     * Terminates the CAS SSO session by destroying the TGT (if any) and removing cookies related to the SSO session.
     *
     * @param context Request context.
     * @return "success"
     */
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
                LOGGER.debug("Destroying SSO session linked to ticket-granting ticket [{}]", tgtId);
                final List<LogoutRequest> logoutRequests = this.centralAuthenticationService.destroyTicketGrantingTicket(tgtId);
                WebUtils.putLogoutRequests(context, logoutRequests);
            }
            LOGGER.debug("Removing CAS cookies");
            this.ticketGrantingTicketCookieGenerator.removeCookie(response);
            this.warnCookieGenerator.removeCookie(response);

            destroyApplicationSession(request, response);
            LOGGER.debug("Terminated all CAS sessions successfully.");
            return this.eventFactorySupport.success(this);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Destroy application session.
     * Also kills all delegated authn profiles via pac4j.
     *
     * @param request  the request
     * @param response the response
     */
    protected void destroyApplicationSession(final HttpServletRequest request, final HttpServletResponse response) {
        LOGGER.debug("Destroying application session");
        final ProfileManager manager = Pac4jUtils.getPac4jProfileManager(request, response);
        manager.logout();

        final HttpSession session = request.getSession();
        if (session != null) {
            session.invalidate();
        }
    }

    private static boolean isLogoutRequestConfirmed(final RequestContext requestContext) {
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        return request.getParameterMap().containsKey("LogoutRequestConfirmed");
    }
}
