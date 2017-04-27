package org.apereo.cas.support.oauth.web;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.profile.OAuthClientProfile;
import org.apereo.cas.support.oauth.profile.OAuthUserProfile;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.OAuthToken;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.apereo.cas.ticket.refreshtoken.RefreshTokenFactory;
import org.apereo.cas.support.oauth.util.OAuthUtils;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * This controller returns an access token according to the given
 * OAuth code and client credentials (authorization code grant type)
 * or according to the refresh token and client credentials
 * (refresh token grant type) or according to the user identity
 * (resource owner password grant type).
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
public class OAuth20AccessTokenController extends BaseOAuthWrapperController {

    @Autowired
    private CasConfigurationProperties casProperties;

    private RefreshTokenFactory refreshTokenFactory;

    private AccessTokenResponseGenerator accessTokenResponseGenerator;

    /**
     * Handle request internal model and view.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @RequestMapping(path = OAuthConstants.BASE_OAUTH20_URL + '/' + OAuthConstants.ACCESS_TOKEN_URL, method = RequestMethod.GET)
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);

        if (!verifyAccessTokenRequest(request, response)) {
            logger.error("Access token request verification fails");
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST);
        }

        final String grantType = request.getParameter(OAuthConstants.GRANT_TYPE);
        final Service service;
        final Authentication authentication;

        final boolean generateRefreshToken;
        final OAuthRegisteredService registeredService;

        final J2EContext context = new J2EContext(request, response);
        final ProfileManager manager = new ProfileManager(context);


        if (isGrantType(grantType, OAuthGrantType.AUTHORIZATION_CODE) || isGrantType(grantType, OAuthGrantType.REFRESH_TOKEN)) {
            final Optional<UserProfile> profile = manager.get(true);
            final String clientId = profile.get().getId();
            registeredService = OAuthUtils.getRegisteredOAuthService(this.servicesManager, clientId);

            // we generate a refresh token if requested by the service but not from a refresh token
            generateRefreshToken = registeredService != null && registeredService.isGenerateRefreshToken()
                    && isGrantType(grantType, OAuthGrantType.AUTHORIZATION_CODE);

            final String parameterName;
            if (isGrantType(grantType, OAuthGrantType.AUTHORIZATION_CODE)) {
                parameterName = OAuthConstants.CODE;
            } else {
                parameterName = OAuthConstants.REFRESH_TOKEN;
            }

            final OAuthToken token = getToken(request, parameterName);
            if (token == null) {
                logger.error("No token found for authorization_code or refresh_token grant types");
                return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_GRANT);
            }
            service = token.getService();
            authentication = token.getAuthentication();

        } else {
            final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
            registeredService = OAuthUtils.getRegisteredOAuthService(this.servicesManager, clientId);
            generateRefreshToken = registeredService != null && registeredService.isGenerateRefreshToken();

            try {
                // resource owner password grant type
                final Optional<OAuthUserProfile> profile = manager.get(true);
                if (!profile.isPresent()) {
                    throw new UnauthorizedServiceException("Oauth user profile cannot be determined");
                }
                service = createService(registeredService);
                authentication = createAuthentication(profile.get(), registeredService, context);

                RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(service,
                        registeredService, authentication);
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
                return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_GRANT);
            }
        }

        final AccessToken accessToken = generateAccessToken(service, authentication, context);
        RefreshToken refreshToken = null;
        if (generateRefreshToken) {
            refreshToken = this.refreshTokenFactory.create(service, authentication);
            this.ticketRegistry.addTicket(refreshToken);
        }

        logger.debug("access token: {} / timeout: {} / refresh token: {}", accessToken,
                casProperties.getTicket().getTgt().getTimeToKillInSeconds(), refreshToken);

        this.accessTokenResponseGenerator.generate(request, response, registeredService, service,
                accessToken, refreshToken,
                casProperties.getTicket().getTgt().getTimeToKillInSeconds());

        response.setStatus(HttpServletResponse.SC_OK);
        return null;
    }

    /**
     * Return the OAuth token (a code or a refresh token).
     *
     * @param request       the HTTP request
     * @param parameterName the parameter name
     * @return the OAuth token
     */
    private OAuthToken getToken(final HttpServletRequest request, final String parameterName) {
        final String codeParameter = request.getParameter(parameterName);
        final OAuthToken token = this.ticketRegistry.getTicket(codeParameter, OAuthToken.class);
        // token should not be expired
        if (token == null || token.isExpired()) {
            logger.error("Code or refresh token expired: {}", token);
            if (token != null) {
                this.ticketRegistry.deleteTicket(token.getId());
            }
            return null;
        }
        if (token instanceof OAuthCode && !(token instanceof RefreshToken)) {
            this.ticketRegistry.deleteTicket(token.getId());
        }

        return token;
    }

    /**
     * Verify the access token request.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return true, if successful
     */
    private boolean verifyAccessTokenRequest(final HttpServletRequest request, final HttpServletResponse response) {

        // must have the right grant type
        final String grantType = request.getParameter(OAuthConstants.GRANT_TYPE);
        if (!checkGrantTypes(grantType, OAuthGrantType.AUTHORIZATION_CODE, OAuthGrantType.PASSWORD, OAuthGrantType.REFRESH_TOKEN)) {
            return false;
        }

        // must be authenticated (client or user)
        final J2EContext context = new J2EContext(request, response);
        final ProfileManager manager = new ProfileManager(context);
        final Optional<UserProfile> profile = manager.get(true);
        if (profile == null || !profile.isPresent()) {
            return false;
        }

        final UserProfile uProfile = profile.get();

        // authorization code grant type
        if (isGrantType(grantType, OAuthGrantType.AUTHORIZATION_CODE)) {
            final String clientId = uProfile.getId();
            final String redirectUri = request.getParameter(OAuthConstants.REDIRECT_URI);
            final OAuthRegisteredService registeredService = OAuthUtils.getRegisteredOAuthService(this.servicesManager, clientId);

            return uProfile instanceof OAuthClientProfile
                    && this.validator.checkParameterExist(request, OAuthConstants.REDIRECT_URI)
                    && this.validator.checkParameterExist(request, OAuthConstants.CODE)
                    && this.validator.checkCallbackValid(registeredService, redirectUri);

        } else if (isGrantType(grantType, OAuthGrantType.REFRESH_TOKEN)) {
            // refresh token grant type
            return uProfile instanceof OAuthClientProfile
                    && this.validator.checkParameterExist(request, OAuthConstants.REFRESH_TOKEN);

        } else {

            final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
            final OAuthRegisteredService registeredService = OAuthUtils.getRegisteredOAuthService(this.servicesManager, clientId);

            // resource owner password grant type
            return uProfile instanceof OAuthUserProfile
                    && this.validator.checkParameterExist(request, OAuthConstants.CLIENT_ID)
                    && this.validator.checkServiceValid(registeredService);
        }
    }

    /**
     * Check the grant type against expected grant types.
     *
     * @param type          the current grant type
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
     * @param type         the given grant type
     * @param expectedType the expected grant type
     * @return whether the grant type is the expected one
     */
    private static boolean isGrantType(final String type, final OAuthGrantType expectedType) {
        return expectedType != null && expectedType.name().toLowerCase().equals(type);
    }

    public void setAccessTokenResponseGenerator(final AccessTokenResponseGenerator accessTokenResponseGenerator) {
        this.accessTokenResponseGenerator = accessTokenResponseGenerator;
    }

    public RefreshTokenFactory getRefreshTokenFactory() {
        return this.refreshTokenFactory;
    }

    public void setRefreshTokenFactory(final RefreshTokenFactory refreshTokenFactory) {
        this.refreshTokenFactory = refreshTokenFactory;
    }


}
