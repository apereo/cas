package org.apereo.cas.web.flow;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CentralAuthenticationService;
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

    private boolean createSsoSessionCookieOnRenewAuthentications = true;
    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;
    private CentralAuthenticationService centralAuthenticationService;
    private ServicesManager servicesManager;
    private AuthenticationSystemSupport authenticationSystemSupport;

    public SendTicketGrantingTicketAction(final CentralAuthenticationService centralAuthenticationService, 
                                          final ServicesManager servicesManager,
                                          final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator,
                                          final AuthenticationSystemSupport authenticationSystemSupport, 
                                          final boolean renewedAuthn) {
        super();
        this.centralAuthenticationService = centralAuthenticationService;
        this.servicesManager = servicesManager;
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
        this.authenticationSystemSupport = authenticationSystemSupport;
        this.createSsoSessionCookieOnRenewAuthentications = renewedAuthn;
    }

    @Override
    protected Event doExecute(final RequestContext context) {
        final String ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(context);
        final String ticketGrantingTicketValueFromCookie = (String) context.getFlowScope().get("ticketGrantingTicketId");

        if (StringUtils.isBlank(ticketGrantingTicketId)) {
            return success();
        }

        if (WebUtils.isAuthenticatingAtPublicWorkstation(context)) {
            LOGGER.info("Authentication is at a public workstation. "
                    + "SSO cookie will not be generated. Subsequent requests will be challenged for authentication.");
        } else if (!this.createSsoSessionCookieOnRenewAuthentications && isAuthenticationRenewed(context)) {
            LOGGER.info("Authentication session is renewed but CAS is not configured to create the SSO session. "
                    + "SSO cookie will not be generated. Subsequent requests will be challenged for credentials.");
        } else {
            LOGGER.debug("Setting TGC for current session.");
            this.ticketGrantingTicketCookieGenerator.addCookie(WebUtils.getHttpServletRequest(context), WebUtils
                .getHttpServletResponse(context), ticketGrantingTicketId);
        }

        if (ticketGrantingTicketValueFromCookie != null && !ticketGrantingTicketId.equals(ticketGrantingTicketValueFromCookie)) {
            this.centralAuthenticationService.destroyTicketGrantingTicket(ticketGrantingTicketValueFromCookie);
        }

        return success();
    }

    /**
     * Tries to determine if authentication was created as part of a "renew" event.
     * Renewed authentications can occur if the service is not allowed to participate
     * in SSO or if a "renew" request parameter is specified.
     *
     * @param ctx the request context
     * @return true if renewed
     */
    private boolean isAuthenticationRenewed(final RequestContext ctx) {
        if (ctx.getRequestParameters().contains(CasProtocolConstants.PARAMETER_RENEW)) {
            LOGGER.debug("[{}] is specified for the request. The authentication session will be considered renewed.", CasProtocolConstants.PARAMETER_RENEW);
            return true;
        }

        final Service service = WebUtils.getService(ctx);
        if (service != null) {
            final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
            if (registeredService != null) {
                final boolean isAllowedForSso = registeredService.getAccessStrategy().isServiceAccessAllowedForSso();
                LOGGER.debug("Located [{}] in registry. Service access to participate in SSO is set to [{}]",
                        registeredService.getServiceId(), isAllowedForSso);
                return !isAllowedForSso;
            }
        }

        return false;
    }
}
