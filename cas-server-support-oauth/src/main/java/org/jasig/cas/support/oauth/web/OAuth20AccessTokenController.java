package org.jasig.cas.support.oauth.web;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.HttpStatus;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.services.RegisteredServiceAccessStrategySupport;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.http.profile.HttpProfile;
import org.pac4j.jwt.JwtConstants;
import org.pac4j.jwt.profile.JwtGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This controller returns an access token according to the service and code (service ticket) given.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Component("accessTokenController")
public final class OAuth20AccessTokenController extends BaseOAuthWrapperController {

    @Autowired
    @Qualifier("accessTokenJwtGenerator")
    private JwtGenerator accessTokenJwtGenerator;

    /**
     * Instantiates a new o auth20 access token controller.
     */
    public OAuth20AccessTokenController() {}

    /**
     * Ensure the encryption secret has been set.
     */
    @PostConstruct
    public void postConstruct() {
        CommonHelper.assertNotNull("encryptionSecret", accessTokenJwtGenerator.getEncryptionSecret());
    }

    @Override
    protected ModelAndView internalHandleRequest(final String method, final HttpServletRequest request,
                                                 final HttpServletResponse response) throws Exception {
        try {

            final String redirectUri = request.getParameter(OAuthConstants.REDIRECT_URI);
            logger.debug("{}: {}", OAuthConstants.REDIRECT_URI, redirectUri);

            final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
            logger.debug("{}: {}", OAuthConstants.CLIENT_ID, clientId);

            final String clientSecret = request.getParameter(OAuthConstants.CLIENT_SECRET);

            final String code = request.getParameter(OAuthConstants.CODE);
            logger.debug("{}: {}", OAuthConstants.CODE, code);

            final boolean isVerified = verifyAccessTokenRequest(response, redirectUri, clientId, clientSecret, code);
            if (!isVerified) {
                return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, HttpStatus.SC_BAD_REQUEST);
            }

            final OAuthRegisteredService service = OAuthUtils.getRegisteredOAuthService(this.servicesManager, clientId);
            final ServiceTicket serviceTicket = (ServiceTicket) ticketRegistry.getTicket(code);
            // service ticket should be valid
            if (serviceTicket == null || serviceTicket.isExpired()) {
                logger.error("Code expired: {}", code);
                return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_GRANT, HttpStatus.SC_BAD_REQUEST);
            }
            final TicketGrantingTicket ticketGrantingTicket = serviceTicket.getGrantingTicket();
            // remove service ticket
            ticketRegistry.deleteTicket(serviceTicket.getId());

            RegisteredServiceAccessStrategySupport.ensurePrincipalAccessIsAllowedForService(serviceTicket, service, ticketGrantingTicket);
            final Principal principal = ticketGrantingTicket.getAuthentication().getPrincipal();
            final Map<String, Object> attributes = new HashMap<>(service.getAttributeReleasePolicy().getAttributes(principal));

            if (attributes.containsKey(JwtConstants.SUBJECT) || attributes.containsKey(JwtConstants.ISSUE_TIME)
                    || attributes.containsKey(JwtConstants.ISSUER) || attributes.containsKey(JwtConstants.AUDIENCE)
                    || attributes.containsKey(JwtConstants.EXPIRATION_TIME)) {
                logger.error("Current profile: {} has forbidden attributes.", principal);
                return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_PROFILE, HttpStatus.SC_BAD_REQUEST);
            }

            attributes.put(JwtConstants.ISSUER, loginUrl.replace("/login$", OAuthConstants.ENDPOINT_OAUTH2));
            attributes.put(JwtConstants.AUDIENCE, service.getServiceId());
            final int expires = (int) (this.timeout - TimeUnit.MILLISECONDS
                    .toSeconds(System.currentTimeMillis() - ticketGrantingTicket.getCreationTime()));
            attributes.put(JwtConstants.EXPIRATION_TIME, DateUtils.addSeconds(new Date(), expires));

            final HttpProfile profile = new HttpProfile();
            profile.setId(principal.getId());
            profile.addAttributes(attributes);
            final String accessToken = this.accessTokenJwtGenerator.generate(profile);

            final String text = String.format("%s=%s&%s=%s", OAuthConstants.ACCESS_TOKEN, accessToken, OAuthConstants.EXPIRES, expires);
            logger.debug("OAuth access token response: {}", text);
            response.setContentType("text/plain");
            return OAuthUtils.writeText(response, text, HttpStatus.SC_OK);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_GRANT, HttpStatus.SC_BAD_REQUEST);
        }
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
        if (service == null || !service.getAccessStrategy().isServiceAccessAllowed()) {
            logger.error("Service {} is not found in the registry or it is disabled.", clientId);
            return false;
        }

        final String serviceId = service.getServiceId();
        if (!redirectUri.matches(serviceId)) {
            logger.error("Unsupported {}: {} for serviceId: {}", OAuthConstants.REDIRECT_URI, redirectUri, serviceId);
            return false;
        }

        if (!StringUtils.equals(service.getClientSecret(), clientSecret)) {
            logger.error("Wrong client secret for service: {}", service);
            return false;
        }
        return true;
    }
}
