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
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.apereo.cas.ticket.refreshtoken.RefreshTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
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

    private final RefreshTokenFactory refreshTokenFactory;
    private final AccessTokenResponseGenerator accessTokenResponseGenerator;


    public OAuth20AccessTokenEndpointController(final ServicesManager servicesManager,
                                                final TicketRegistry ticketRegistry,
                                                final OAuth20Validator validator,
                                                final AccessTokenFactory accessTokenFactory,
                                                final PrincipalFactory principalFactory,
                                                final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                                final RefreshTokenFactory refreshTokenFactory,
                                                final AccessTokenResponseGenerator accessTokenResponseGenerator,
                                                final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter,
                                                final CasConfigurationProperties casProperties,
                                                final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator) {
        super(servicesManager, ticketRegistry, validator, accessTokenFactory,
                principalFactory, webApplicationServiceServiceFactory,
                scopeToAttributesFilter, casProperties, ticketGrantingTicketCookieGenerator);
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

            final Service service;
            final Authentication authentication;
            OAuthToken token = null;
            final boolean generateRefreshToken;
            final OAuthRegisteredService registeredService;

            final J2EContext context = WebUtils.getPac4jJ2EContext(request, response);
            final ProfileManager manager = WebUtils.getPac4jProfileManager(request, response);
            final String grantType = request.getParameter(OAuthConstants.GRANT_TYPE);
            LOGGER.debug("OAuth grant type is [{}]", grantType);
            if (OAuthUtils.isGrantType(grantType, OAuth20GrantTypes.AUTHORIZATION_CODE) || OAuthUtils.isGrantType(grantType, OAuth20GrantTypes.REFRESH_TOKEN)) {
                final Optional<UserProfile> profile = manager.get(true);
                final String clientId = profile.get().getId();
                registeredService = OAuthUtils.getRegisteredOAuthService(getServicesManager(), clientId);
                LOGGER.debug("Located OAuth registered service [{}]", registeredService);

                // we generate a refresh token if requested by the service but not from a refresh token
                generateRefreshToken = registeredService != null && registeredService.isGenerateRefreshToken()
                        && OAuthUtils.isGrantType(grantType, OAuth20GrantTypes.AUTHORIZATION_CODE);

                final String parameterName;
                if (OAuthUtils.isGrantType(grantType, OAuth20GrantTypes.AUTHORIZATION_CODE)) {
                    parameterName = OAuthConstants.CODE;
                } else {
                    parameterName = OAuthConstants.REFRESH_TOKEN;
                }

                LOGGER.debug("Locating OAuth token via request parameter [{}]", parameterName);
                token = getToken(request, parameterName);

                if (token == null) {
                    LOGGER.error("No token found for authorization_code or refresh_token grant types");
                    return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_GRANT);
                }
                LOGGER.debug("Found OAuth token [{}]", token);
                service = token.getService();
                authentication = token.getAuthentication();

            } else {
                // resource owner password grant type
                final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
                LOGGER.debug("Locating OAuth registered service by client id [{}]", clientId);

                registeredService = OAuthUtils.getRegisteredOAuthService(getServicesManager(), clientId);
                LOGGER.debug("Located OAuth registered service [{}]", registeredService);

                generateRefreshToken = registeredService != null && registeredService.isGenerateRefreshToken();

                try {
                    final Optional<OAuthUserProfile> profile = manager.get(true);
                    if (!profile.isPresent()) {
                        throw new UnauthorizedServiceException("OAuth user profile cannot be determined");
                    }
                    LOGGER.debug("Creating matching service request based on [{}]", registeredService);
                    service = createService(registeredService, context);

                    LOGGER.debug("Authenticating the OAuth request indicated by [{}]", service);
                    authentication = createAuthentication(profile.get(), registeredService, context, service);

                    RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(service, registeredService, authentication);
                } catch (final Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    return OAuthUtils.writeTextError(response, OAuthConstants.INVALID_GRANT);
                }
            }
            LOGGER.debug("Creating access token for [{}]", service);

            final TicketGrantingTicket tgt = token != null ? token.getGrantingTicket() : null;
            final AccessToken accessToken = generateAccessToken(service, authentication, context, tgt);

            RefreshToken refreshToken = null;
            if (generateRefreshToken) {
                LOGGER.debug("Creating refresh token for [{}]", service);
                refreshToken = this.refreshTokenFactory.create(service, authentication, tgt);
                addTicketToRegistry(refreshToken, tgt);
            }
            LOGGER.debug("Access token: [{}] / Timeout: [{}] (Seconds) / Refresh Token: [{}]", accessToken,
                    casProperties.getTicket().getTgt().getTimeToKillInSeconds(), refreshToken);

            final String responseType = context.getRequestParameter(OAuthConstants.RESPONSE_TYPE);
            final OAuth20ResponseTypes type = Arrays.stream(OAuth20ResponseTypes.values())
                    .filter(t -> t.getType().equalsIgnoreCase(responseType))
                    .findFirst().orElse(OAuth20ResponseTypes.CODE);
            LOGGER.debug("OAuth response type is [{}]", type);

            this.accessTokenResponseGenerator.generate(request, response, registeredService, service,
                    accessToken, refreshToken, casProperties.getTicket().getTgt().getTimeToKillInSeconds(), type);

            LOGGER.debug("Adding OAuth access token [{}] to the registry", accessToken);
            addTicketToRegistry(accessToken, tgt);

            response.setStatus(HttpServletResponse.SC_OK);
            return null;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw Throwables.propagate(e);
        }
    }

    private void addTicketToRegistry(final OAuthToken ticket, final TicketGrantingTicket ticketGrantingTicket) {
        getTicketRegistry().addTicket(ticket);
        if (ticketGrantingTicket != null) {
            getTicketRegistry().updateTicket(ticketGrantingTicket);
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
        if (OAuthUtils.isGrantType(grantType, OAuth20GrantTypes.AUTHORIZATION_CODE)) {
            final String clientId = uProfile.getId();
            final String redirectUri = request.getParameter(OAuthConstants.REDIRECT_URI);
            final OAuthRegisteredService registeredService = OAuthUtils.getRegisteredOAuthService(getServicesManager(), clientId);

            return uProfile instanceof OAuthClientProfile
                    && getValidator().checkParameterExist(request, OAuthConstants.REDIRECT_URI)
                    && getValidator().checkParameterExist(request, OAuthConstants.CODE)
                    && getValidator().checkCallbackValid(registeredService, redirectUri);

        } else if (OAuthUtils.isGrantType(grantType, OAuth20GrantTypes.REFRESH_TOKEN)) {
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
            if (OAuthUtils.isGrantType(type, expectedType)) {
                return true;
            }
        }
        LOGGER.error("Unsupported grant type: [{}]", type);
        return false;
    }
}
