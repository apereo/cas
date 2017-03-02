package org.apereo.cas.support.oauth.web.endpoints;

import com.google.common.base.Throwables;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.profile.OAuthClientProfile;
import org.apereo.cas.support.oauth.profile.OAuthUserProfile;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuthUtils;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;
import org.apereo.cas.support.oauth.web.AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.BaseOAuthWrapperController;
import org.apereo.cas.ticket.OAuthToken;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.apereo.cas.ticket.refreshtoken.RefreshTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
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
public class OAuth20AccessTokenEndpointController extends BaseOAuthWrapperController {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20AccessTokenEndpointController.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    private RefreshTokenFactory refreshTokenFactory;
    private AccessTokenResponseGenerator accessTokenResponseGenerator;

    public OAuth20AccessTokenEndpointController(final ServicesManager servicesManager,
                                                final TicketRegistry ticketRegistry,
                                                final OAuth20Validator validator,
                                                final AccessTokenFactory accessTokenFactory,
                                                final PrincipalFactory principalFactory,
                                                final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                                final RefreshTokenFactory refreshTokenFactory,
                                                final AccessTokenResponseGenerator accessTokenResponseGenerator,
                                                final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter,
                                                final CasConfigurationProperties casProperties) {
        super(servicesManager, ticketRegistry, validator, accessTokenFactory,
                principalFactory, webApplicationServiceServiceFactory, scopeToAttributesFilter, casProperties);
        this.refreshTokenFactory = refreshTokenFactory;
        this.accessTokenResponseGenerator = accessTokenResponseGenerator;
    }

    /**
     * Handle request internal model and view.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @PostMapping(path = OAuthConstants.BASE_OAUTH20_URL + '/' + OAuthConstants.ACCESS_TOKEN_URL)
    public ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        try {
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);

            if (!verifyAccessTokenRequest(request, response)) {
                LOGGER.error("Access token request verification fails");
                return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_REQUEST);
            }

            final String grantType = request.getParameter(OAuthConstants.GRANT_TYPE);
            final Service service;
            final Authentication authentication;

            final boolean generateRefreshToken;
            final OAuthRegisteredService registeredService;

            final J2EContext context = WebUtils.getPac4jJ2EContext(request, response);
            final ProfileManager manager = WebUtils.getPac4jProfileManager(request, response);

            if (isGrantType(grantType, OAuth20GrantTypes.AUTHORIZATION_CODE) || isGrantType(grantType, OAuth20GrantTypes.REFRESH_TOKEN)) {
                final Optional<UserProfile> profile = manager.get(true);
                final String clientId = profile.get().getId();
                registeredService = OAuthUtils.getRegisteredOAuthService(getServicesManager(), clientId);

                // we generate a refresh token if requested by the service but not from a refresh token
                generateRefreshToken = registeredService != null && registeredService.isGenerateRefreshToken()
                        && isGrantType(grantType, OAuth20GrantTypes.AUTHORIZATION_CODE);

                final String parameterName;
                if (isGrantType(grantType, OAuth20GrantTypes.AUTHORIZATION_CODE)) {
                    parameterName = OAuthConstants.CODE;
                } else {
                    parameterName = OAuthConstants.REFRESH_TOKEN;
                }

                final OAuthToken token = getToken(request, parameterName);
                if (token == null) {
                    LOGGER.error("No token found for authorization_code or refresh_token grant types");
                    return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_GRANT);
                }
                service = token.getService();
                authentication = token.getAuthentication();

            } else {
                final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
                registeredService = OAuthUtils.getRegisteredOAuthService(getServicesManager(), clientId);
                generateRefreshToken = registeredService != null && registeredService.isGenerateRefreshToken();

                try {
                    // resource owner password grant type
                    final Optional<OAuthUserProfile> profile = manager.get(true);
                    if (!profile.isPresent()) {
                        throw new UnauthorizedServiceException("OAuth user profile cannot be determined");
                    }
                    service = createService(registeredService, context);
                    authentication = createAuthentication(profile.get(), registeredService, context, service);

                    RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(service, registeredService, authentication);
                } catch (final Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_GRANT);
                }
            }

            final AccessToken accessToken = generateAccessToken(service, authentication, context);
            RefreshToken refreshToken = null;
            if (generateRefreshToken) {
                refreshToken = this.refreshTokenFactory.create(service, authentication);
                getTicketRegistry().addTicket(refreshToken);
            }

            LOGGER.debug("access token: [{}] / timeout: [{}] / refresh token: [{}]", accessToken,
                    casProperties.getTicket().getTgt().getTimeToKillInSeconds(), refreshToken);

            final String responseType = context.getRequestParameter(OAuthConstants.RESPONSE_TYPE);
            final OAuth20ResponseTypes type = Arrays.stream(OAuth20ResponseTypes.values())
                    .filter(t -> t.getType().equalsIgnoreCase(responseType))
                    .findFirst().orElse(OAuth20ResponseTypes.CODE);

            this.accessTokenResponseGenerator.generate(request, response, registeredService, service,
                    accessToken, refreshToken, casProperties.getTicket().getTgt().getTimeToKillInSeconds(), type);

            getTicketRegistry().addTicket(accessToken);
            
            response.setStatus(HttpServletResponse.SC_OK);
            return null;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw Throwables.propagate(e);
        }
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
        final OAuthToken token = getTicketRegistry().getTicket(codeParameter, OAuthToken.class);
        // token should not be expired
        if (token == null || token.isExpired()) {
            LOGGER.error("Code or refresh token expired: [{}]", token);
            if (token != null) {
                getTicketRegistry().deleteTicket(token.getId());
            }
            return null;
        }
        if (token instanceof OAuthCode && !(token instanceof RefreshToken)) {
            getTicketRegistry().deleteTicket(token.getId());
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
        if (!checkGrantTypes(grantType, OAuth20GrantTypes.AUTHORIZATION_CODE, OAuth20GrantTypes.PASSWORD, OAuth20GrantTypes.REFRESH_TOKEN)) {
            return false;
        }

        // must be authenticated (client or user)
        final J2EContext context = WebUtils.getPac4jJ2EContext(request, response);
        final ProfileManager manager = WebUtils.getPac4jProfileManager(request, response);
        final Optional<UserProfile> profile = manager.get(true);
        if (profile == null || !profile.isPresent()) {
            return false;
        }

        final UserProfile uProfile = profile.get();

        // authorization code grant type
        if (isGrantType(grantType, OAuth20GrantTypes.AUTHORIZATION_CODE)) {
            final String clientId = uProfile.getId();
            final String redirectUri = request.getParameter(OAuthConstants.REDIRECT_URI);
            final OAuthRegisteredService registeredService = OAuthUtils.getRegisteredOAuthService(getServicesManager(), clientId);

            return uProfile instanceof OAuthClientProfile
                    && getValidator().checkParameterExist(request, OAuthConstants.REDIRECT_URI)
                    && getValidator().checkParameterExist(request, OAuthConstants.CODE)
                    && getValidator().checkCallbackValid(registeredService, redirectUri);

        } else if (isGrantType(grantType, OAuth20GrantTypes.REFRESH_TOKEN)) {
            // refresh token grant type
            return uProfile instanceof OAuthClientProfile
                    && getValidator().checkParameterExist(request, OAuthConstants.REFRESH_TOKEN);

        } else {

            final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
            final OAuthRegisteredService registeredService = OAuthUtils.getRegisteredOAuthService(getServicesManager(), clientId);

            // resource owner password grant type
            return uProfile instanceof OAuthUserProfile
                    && getValidator().checkParameterExist(request, OAuthConstants.CLIENT_ID)
                    && getValidator().checkServiceValid(registeredService);
        }
    }

    /**
     * Check the grant type against expected grant types.
     *
     * @param type          the current grant type
     * @param expectedTypes the expected grant types
     * @return whether the grant type is supported
     */
    private boolean checkGrantTypes(final String type, final OAuth20GrantTypes... expectedTypes) {
        LOGGER.debug("Grant type: [{}]", type);

        for (final OAuth20GrantTypes expectedType : expectedTypes) {
            if (isGrantType(type, expectedType)) {
                return true;
            }
        }
        LOGGER.error("Unsupported grant type: [{}]", type);
        return false;
    }

    /**
     * Check the grant type against an expected grant type.
     *
     * @param type         the given grant type
     * @param expectedType the expected grant type
     * @return whether the grant type is the expected one
     */
    private static boolean isGrantType(final String type, final OAuth20GrantTypes expectedType) {
        return expectedType.name().equalsIgnoreCase(type);
    }
}
