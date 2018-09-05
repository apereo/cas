package org.apereo.cas.web.flow.logout;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.model.core.logout.LogoutProperties;
import org.apereo.cas.logout.LogoutRequest;
import org.apereo.cas.util.Pac4jUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.profile.ProfileManager;
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
@Slf4j
@RequiredArgsConstructor
public class TerminateSessionAction extends AbstractAction {

    /**
     * Parameter to indicate logout request is confirmed.
     */
    public static final String REQUEST_PARAM_LOGOUT_REQUEST_CONFIRMED = "LogoutRequestConfirmed";

    /** The event factory. */
    protected final EventFactorySupport eventFactorySupport = new EventFactorySupport();
    /** The authentication service. */
    protected final CentralAuthenticationService centralAuthenticationService;
    /** The TGT cookie generator. */
    protected final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;
    /** The warn cookie generator. */
    protected final CookieRetrievingCookieGenerator warnCookieGenerator;
    /** The logout properties. */
    protected final LogoutProperties logoutProperties;

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
    @SneakyThrows
    public Event terminate(final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        final HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);

        String tgtId = WebUtils.getTicketGrantingTicketId(context);
        if (StringUtils.isBlank(tgtId)) {
            tgtId = this.ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        }
        if (StringUtils.isNotBlank(tgtId)) {
            LOGGER.debug("Destroying SSO session linked to ticket-granting ticket [{}]", tgtId);
            final List<LogoutRequest> logoutRequests = this.centralAuthenticationService.destroyTicketGrantingTicket(tgtId);
            WebUtils.putLogoutRequests(context, logoutRequests);
        }
        LOGGER.debug("Removing CAS cookies");
        this.ticketGrantingTicketCookieGenerator.removeCookie(response);
        this.warnCookieGenerator.removeCookie(response);

        destroyApplicationSession(request, response);
        LOGGER.debug("Terminated all CAS sessions successfully.");

        if (StringUtils.isNotBlank(logoutProperties.getRedirectUrl())) {
            WebUtils.putLogoutRedirectUrl(context, logoutProperties.getRedirectUrl());
            return this.eventFactorySupport.event(this, CasWebflowConstants.STATE_ID_REDIRECT);
        }

        return this.eventFactorySupport.success(this);
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

        final HttpSession session = request.getSession(false);
        if (session != null) {
            final Object requestedUrl = session.getAttribute(Pac4jConstants.REQUESTED_URL);
            session.invalidate();
            if (requestedUrl != null && !requestedUrl.equals("")) {
                request.getSession(true).setAttribute(Pac4jConstants.REQUESTED_URL, requestedUrl);
            }
        }
    }

    /**
     * Check if the logout must be confirmed.
     *
     * @param requestContext the request context
     * @return if the logout must be confirmed
     */
    protected static boolean isLogoutRequestConfirmed(final RequestContext requestContext) {
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        return request.getParameterMap().containsKey(REQUEST_PARAM_LOGOUT_REQUEST_CONFIRMED);
    }
}
