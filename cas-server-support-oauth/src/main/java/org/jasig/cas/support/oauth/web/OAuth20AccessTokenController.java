package org.jasig.cas.support.oauth.web;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.profile.OAuthClientProfile;
import org.jasig.cas.support.oauth.profile.OAuthUserProfile;
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

        if (!verifyAccessTokenRequest(request, response)) {
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST, HttpStatus.SC_BAD_REQUEST);
        }

        final String codeParameter = request.getParameter(OAuthConstants.CODE);
        final OAuthCode code = ticketRegistry.getTicket(codeParameter, OAuthCode.class);
        // code should not be expired
        if (code == null || code.isExpired()) {
            logger.error("Code expired: {}", code);
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_GRANT, HttpStatus.SC_BAD_REQUEST);
        }
        ticketRegistry.deleteTicket(code.getId());

        final Service service = code.getService();
        final Authentication authentication = code.getAuthentication();
        final AccessToken accessToken = generateAccessToken(service, authentication);

        final String text = String.format("%s=%s&%s=%s", OAuthConstants.ACCESS_TOKEN, accessToken.getId(), OAuthConstants.EXPIRES, timeout);
        logger.debug("OAuth access token response: {}", text);
        response.setContentType("text/plain");
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
        if (OAuthGrantType.AUTHORIZATION_CODE.name().equalsIgnoreCase(grantType)) {

            final String clientId = profile.getId();
            final String redirectUri = request.getParameter(OAuthConstants.REDIRECT_URI);

            return profile instanceof OAuthClientProfile
                    && validator.checkParameterExist(request, OAuthConstants.REDIRECT_URI)
                    && validator.checkParameterExist(request, OAuthConstants.CODE)
                    && validator.checkCallbackValid(clientId, redirectUri);

        } else {

            // resource owner password grant type
            return profile instanceof OAuthUserProfile;
        }
    }

    /**
     * Check the grant type.
     *
     * @param type the current grant type
     * @param expectedTypes the expected grant types
     * @return whether the grant type is supported
     */
    private boolean checkGrantTypes(final String type, final OAuthGrantType... expectedTypes) {
        logger.debug("Grant type: {}", type);

        for (final OAuthGrantType expectedType : expectedTypes) {
            if (StringUtils.equals(type, expectedType.name().toLowerCase())) {
                return true;
            }
        }
        logger.error("Unsupported grant type: {}", type);
        return false;
    }
}
