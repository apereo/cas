package org.jasig.cas.support.oauth.web;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.support.oauth.OAuthWebApplicationService;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * This controller returns an access token which is the CAS
 * granting ticket according to the service and code (service ticket) given.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Component("accessTokenController")
public final class OAuth20AccessTokenController extends BaseOAuthWrapperController {

    @Autowired
    @Qualifier("defaultAccessTokenGenerator")
    private AccessTokenGenerator accessTokenGenerator;

    /**
     * Instantiates a new o auth20 access token controller.
     */
    public OAuth20AccessTokenController() {}

    @Override
    protected ModelAndView internalHandleRequest(final String method, final HttpServletRequest request,
                                                 final HttpServletResponse response) throws Exception {

        final String redirectUri = request.getParameter(OAuthConstants.REDIRECT_URI);
        logger.debug("{} : {}", OAuthConstants.REDIRECT_URI, redirectUri);

        final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
        logger.debug("{} : {}", OAuthConstants.CLIENT_ID, clientId);

        final String clientSecret = request.getParameter(OAuthConstants.CLIENT_SECRET);

        final String code = request.getParameter(OAuthConstants.CODE);
        logger.debug("{} : {}", OAuthConstants.CODE, code);

        final boolean isVerified = verifyAccessTokenRequest(response, redirectUri, clientId, clientSecret, code);
        if (!isVerified) {
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, HttpStatus.SC_BAD_REQUEST);
        }

        final ServiceTicket serviceTicket = (ServiceTicket) ticketRegistry.getTicket(code);
        // service ticket should be valid
        if (serviceTicket == null || serviceTicket.isExpired()) {
            logger.error("Code expired : {}", code);
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_GRANT, HttpStatus.SC_BAD_REQUEST);
        }
        final TicketGrantingTicket ticketGrantingTicket = serviceTicket.getGrantingTicket();
        // remove service ticket
        ticketRegistry.deleteTicket(serviceTicket.getId());

        final OAuthRegisteredService registeredService = OAuthUtils.getRegisteredOAuthService(this.servicesManager, clientId);
        final OAuthWebApplicationService service = new OAuthWebApplicationService(registeredService.getId());
        final String accessTokenEncoded = this.accessTokenGenerator.generate(service, ticketGrantingTicket);
        final int expires = (int) (this.timeout - TimeUnit.MILLISECONDS
                .toSeconds(System.currentTimeMillis() - ticketGrantingTicket.getCreationTime()));
        final String text = String.format("%s=%s&%s=%s", OAuthConstants.ACCESS_TOKEN, accessTokenEncoded, OAuthConstants.EXPIRES, expires);
        logger.debug("OAuth access token response: {}", text);
        response.setContentType("text/plain");
        return OAuthUtils.writeText(response, text, HttpStatus.SC_OK);
    }

    /**
     * Verify access token request by reviewing the values of
     * client id, redirect uri, client secret, code, etc.
     *
     * @param response the response
     * @param redirectUri the redirect uri
     * @param clientId the client id
     * @param clientSecret the client secret
     * @param code the code
     * @return true, if successful
     */
    private boolean verifyAccessTokenRequest(final HttpServletResponse response, final String redirectUri,
                                             final String clientId, final String clientSecret, final String code) {

        // clientId is required
        if (StringUtils.isBlank(clientId)) {
            logger.error("Missing {}", OAuthConstants.CLIENT_ID);
            return false;
        }
        // redirectUri is required
        if (StringUtils.isBlank(redirectUri)) {
            logger.error("Missing {}", OAuthConstants.REDIRECT_URI);
            return false;
        }
        // clientSecret is required
        if (StringUtils.isBlank(clientSecret)) {
            logger.error("Missing {}", OAuthConstants.CLIENT_SECRET);
            return false;
        }
        // code is required
        if (StringUtils.isBlank(code)) {
            logger.error("Missing {}", OAuthConstants.CODE);
            return false;
        }

        final OAuthRegisteredService service = OAuthUtils.getRegisteredOAuthService(this.servicesManager, clientId);
        if (service == null) {
            logger.error("Unknown {} : {}", OAuthConstants.CLIENT_ID, clientId);
            return false;
        }

        final String serviceId = service.getServiceId();
        if (!redirectUri.matches(serviceId)) {
            logger.error("Unsupported {} : {} for serviceId : {}", OAuthConstants.REDIRECT_URI, redirectUri, serviceId);
            return false;
        }

        if (!StringUtils.equals(service.getClientSecret(), clientSecret)) {
            logger.error("Wrong client secret for service {}", service);
            return false;
        }
        return true;
    }
}
