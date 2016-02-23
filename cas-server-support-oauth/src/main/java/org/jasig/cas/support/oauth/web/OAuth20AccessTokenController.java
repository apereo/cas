package org.jasig.cas.support.oauth.web;

import org.apache.http.HttpStatus;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.profile.OAuthClientProfile;
import org.jasig.cas.support.oauth.profile.OAuthUserProfile;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.support.oauth.util.OAuthUtils;
import org.jasig.cas.support.oauth.ticket.accesstoken.AccessToken;
import org.jasig.cas.support.oauth.ticket.code.OAuthCode;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This controller returns an access token according to the given OAuth code and client credentials (authorization code grant type)
 * or according to the user identity (resource owner password grant type).
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Component("accessTokenController")
public final class OAuth20AccessTokenController extends BaseOAuthWrapperController {

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        response.setContentType("text/plain");

        if (!verifyAccessTokenRequest(request, response)) {
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST);
        }

        final String grantType = request.getParameter(OAuthConstants.GRANT_TYPE);
        final Service service;
        final Authentication authentication;
        // authorization code grant type
        if (isGrantType(grantType, OAuthGrantType.AUTHORIZATION_CODE)) {
            final String codeParameter = request.getParameter(OAuthConstants.CODE);
            final OAuthCode code = ticketRegistry.getTicket(codeParameter, OAuthCode.class);
            // code should not be expired
            if (code == null || code.isExpired()) {
                logger.error("Code expired: {}", code);
                if (code != null) {
                    ticketRegistry.deleteTicket(code.getId());
                }
                return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_GRANT);
            }
            ticketRegistry.deleteTicket(code.getId());

            service = code.getService();
            authentication = code.getAuthentication();
        } else {
            // resource owner password grant type
            final J2EContext context = new J2EContext(request, response);
            final ProfileManager manager = new ProfileManager(context);
            final UserProfile profile = manager.get(true);
            final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
            service = createService(OAuthUtils.getRegisteredOAuthService(this.servicesManager, clientId));
            authentication = createAuthentication(profile);
        }
        final AccessToken accessToken = generateAccessToken(service, authentication);

        final String text = String.format("%s=%s&%s=%s", OAuthConstants.ACCESS_TOKEN, accessToken.getId(), OAuthConstants.EXPIRES, timeout);
        logger.debug("OAuth access token response: {}", text);
        return OAuthUtils.writeText(response, text, HttpStatus.SC_OK);
    }

    /**
     * Verify the access token request.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @return true, if successful
     */
    private boolean verifyAccessTokenRequest(final HttpServletRequest request, final HttpServletResponse response) {

        // must have the right grant type
        final String grantType = request.getParameter(OAuthConstants.GRANT_TYPE);
        if (!checkGrantTypes(grantType, OAuthGrantType.AUTHORIZATION_CODE, OAuthGrantType.PASSWORD)) {
            return false;
        }

        // must be authenticated (client or user)
        final J2EContext context = new J2EContext(request, response);
        final ProfileManager manager = new ProfileManager(context);
        final UserProfile profile = manager.get(true);
        if (profile == null) {
            return false;
        }

        // authorization code grant type
        if (isGrantType(grantType, OAuthGrantType.AUTHORIZATION_CODE)) {

            final String clientId = profile.getId();
            final String redirectUri = request.getParameter(OAuthConstants.REDIRECT_URI);
            final OAuthRegisteredService registeredService = OAuthUtils.getRegisteredOAuthService(this.servicesManager, clientId);

            return profile instanceof OAuthClientProfile
                    && validator.checkParameterExist(request, OAuthConstants.REDIRECT_URI)
                    && validator.checkParameterExist(request, OAuthConstants.CODE)
                    && validator.checkCallbackValid(registeredService, redirectUri);

        } else {

            final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
            final OAuthRegisteredService registeredService = OAuthUtils.getRegisteredOAuthService(this.servicesManager, clientId);

            // resource owner password grant type
            return profile instanceof OAuthUserProfile
                    && validator.checkParameterExist(request, OAuthConstants.CLIENT_ID)
                    && validator.checkServiceValid(registeredService);
        }
    }

    /**
     * Check the grant type against expected grant types.
     *
     * @param type the current grant type
     * @param expectedTypes the expected grant types
     * @return whether the grant type is supported
     */
    private boolean checkGrantTypes(final String type, final OAuthGrantType... expectedTypes) {
        logger.debug("Grant type: {}", type);

        for (final OAuthGrantType expectedType : expectedTypes) {
            if (isGrantType(type, expectedType)) {
                return true;
            }
        }
        logger.error("Unsupported grant type: {}", type);
        return false;
    }

    /**
     * Check the grant type against an expected grant type.
     *
     * @param type the given grant type
     * @param expectedType the expected grant type
     * @return whether the grant type is the expected one
     */
    private boolean isGrantType(final String type, final OAuthGrantType expectedType) {
        return expectedType != null && expectedType.name().toLowerCase().equals(type);
    }
}
