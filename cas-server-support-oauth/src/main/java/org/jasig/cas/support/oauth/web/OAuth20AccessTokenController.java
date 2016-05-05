package org.jasig.cas.support.oauth.web;

import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.http.HttpStatus;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.PrincipalException;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.RegisteredServiceAccessStrategyUtils;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.profile.OAuthClientProfile;
import org.jasig.cas.support.oauth.profile.OAuthUserProfile;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.support.oauth.ticket.OAuthToken;
import org.jasig.cas.support.oauth.ticket.accesstoken.AccessToken;
import org.jasig.cas.support.oauth.ticket.code.OAuthCode;
import org.jasig.cas.support.oauth.ticket.refreshtoken.RefreshToken;
import org.jasig.cas.support.oauth.ticket.refreshtoken.RefreshTokenFactory;
import org.jasig.cas.support.oauth.util.OAuthUtils;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.CommonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * This controller returns an access token according to the given OAuth code and client credentials (authorization code grant type)
 * or according to the refresh token and client credentials (refresh token grant type) or according to the user identity
 * (resource owner password grant type).
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@RefreshScope
@Controller("accessTokenController")
public class OAuth20AccessTokenController extends BaseOAuthWrapperController {

    @Autowired
    @Qualifier("defaultRefreshTokenFactory")
    private RefreshTokenFactory refreshTokenFactory;

    @RequestMapping(path = OAuthConstants.BASE_OAUTH20_URL + '/' + OAuthConstants.ACCESS_TOKEN_URL, method = RequestMethod.POST)
    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        response.setContentType("text/plain");

        if (!verifyAccessTokenRequest(request, response)) {
            logger.error("Access token request verification fails");
            return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST);
        }

        final String grantType = request.getParameter(OAuthConstants.GRANT_TYPE);
        final Service service;
        final Authentication authentication;
        final boolean generateRefreshToken;
        final boolean jsonFormat;
        // authorization code and refresh token grant types
        if (isGrantType(grantType, OAuthGrantType.AUTHORIZATION_CODE) || isGrantType(grantType, OAuthGrantType.REFRESH_TOKEN)) {

            final J2EContext context = new J2EContext(request, response);
            final ProfileManager manager = new ProfileManager(context);
            final Optional<UserProfile> profile = manager.get(true);
            final String clientId = profile.get().getId();
            final OAuthRegisteredService registeredService = OAuthUtils.getRegisteredOAuthService(this.servicesManager, clientId);
            // we generate a refresh token if requested by the service but not from a refresh token
            generateRefreshToken = registeredService.isGenerateRefreshToken() 
                    && isGrantType(grantType, OAuthGrantType.AUTHORIZATION_CODE);
            
            jsonFormat = registeredService.isJsonFormat();

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
            final OAuthRegisteredService registeredService = OAuthUtils.getRegisteredOAuthService(this.servicesManager, clientId);
            generateRefreshToken = registeredService.isGenerateRefreshToken();
            jsonFormat = registeredService.isJsonFormat();

            // resource owner password grant type
            final J2EContext context = new J2EContext(request, response);
            final ProfileManager manager = new ProfileManager(context);
            final Optional<OAuthUserProfile>  profile = manager.get(true);
            service = createService(registeredService);
            authentication = createAuthentication(profile.get(), registeredService);

            try {
                RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(service, 
                        registeredService, authentication);
            } catch (final UnauthorizedServiceException | PrincipalException e) {
                logger.error(e.getMessage(), e);
                return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_GRANT);
            }
        }

        final AccessToken accessToken = generateAccessToken(service, authentication);
        final String accessTokenId = accessToken.getId();
        String refreshTokenId = null;
        if (generateRefreshToken) {
            final RefreshToken refreshToken = this.refreshTokenFactory.create(service, authentication);
            this.ticketRegistry.addTicket(refreshToken);
            refreshTokenId = refreshToken.getId();
        }

        logger.debug("access token: {} / timeout: {} / refresh token: {}", accessTokenId, this.timeout, refreshTokenId);
        if (jsonFormat) {
            response.setContentType("application/json");
            try (final JsonGenerator jsonGenerator = this.jsonFactory.createGenerator(response.getWriter())) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField(OAuthConstants.ACCESS_TOKEN, accessTokenId);
                jsonGenerator.writeNumberField(OAuthConstants.EXPIRES, this.timeout);
                if (CommonHelper.isNotBlank(refreshTokenId)) {
                    jsonGenerator.writeStringField(OAuthConstants.REFRESH_TOKEN, refreshTokenId);
                }
                jsonGenerator.writeEndObject();
            }
            return null;
        } else {
            String text = String.format("%s=%s&%s=%s", OAuthConstants.ACCESS_TOKEN, accessTokenId, OAuthConstants.EXPIRES, this.timeout);
            if (CommonHelper.isNotBlank(refreshTokenId)) {
                text += '&' + OAuthConstants.REFRESH_TOKEN + '=' + refreshTokenId;
            }
            return OAuthUtils.writeText(response, text, HttpStatus.SC_OK);
        }
    }

    /**
     * Return the OAuth token (a code or a refresh token).
     *
     * @param request the HTTP request
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
     * @param request the HTTP request
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
        final Optional<UserProfile> profiled = manager.get(true);
        if (!profiled.isPresent()) {
            return false;
        }

        final UserProfile profile = profiled.get();
        
        // authorization code grant type
        if (isGrantType(grantType, OAuthGrantType.AUTHORIZATION_CODE)) {

            final String clientId = profile.getId();
            final String redirectUri = request.getParameter(OAuthConstants.REDIRECT_URI);
            final OAuthRegisteredService registeredService = OAuthUtils.getRegisteredOAuthService(this.servicesManager, clientId);

            return profile instanceof OAuthClientProfile
                    && this.validator.checkParameterExist(request, OAuthConstants.REDIRECT_URI)
                    && this.validator.checkParameterExist(request, OAuthConstants.CODE)
                    && this.validator.checkCallbackValid(registeredService, redirectUri);

        } else if (isGrantType(grantType, OAuthGrantType.REFRESH_TOKEN)){
            // refresh token grant type
            return profile instanceof OAuthClientProfile
                    && this.validator.checkParameterExist(request, OAuthConstants.REFRESH_TOKEN);

        } else {

            final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
            final OAuthRegisteredService registeredService = OAuthUtils.getRegisteredOAuthService(this.servicesManager, clientId);

            // resource owner password grant type
            return profile instanceof OAuthUserProfile
                    && this.validator.checkParameterExist(request, OAuthConstants.CLIENT_ID)
                    && this.validator.checkServiceValid(registeredService);
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

    public RefreshTokenFactory getRefreshTokenFactory() {
        return this.refreshTokenFactory;
    }

    public void setRefreshTokenFactory(final RefreshTokenFactory refreshTokenFactory) {
        this.refreshTokenFactory = refreshTokenFactory;
    }
}
