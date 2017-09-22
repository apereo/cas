package org.apereo.cas.support.oauth.web.endpoints;

import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.authenticator.OAuth20CasAuthenticationBuilder;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.profile.OAuthClientProfile;
import org.apereo.cas.support.oauth.profile.OAuthUserProfile;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;
import org.apereo.cas.support.oauth.web.response.accesstoken.AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenAuthorizationCodeGrantRequestExtractor;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenPasswordGrantRequestExtractor;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRefreshTokenGrantRequestExtractor;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.BaseAccessTokenGrantRequestExtractor;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
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
public class OAuth20AccessTokenEndpointController extends BaseOAuth20Controller {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20AccessTokenEndpointController.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    private final RefreshTokenFactory refreshTokenFactory;
    private final AccessTokenResponseGenerator accessTokenResponseGenerator;
    private final OAuth20CasAuthenticationBuilder authenticationBuilder;
    private final CentralAuthenticationService centralAuthenticationService;
    
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
                                                final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator,
                                                final OAuth20CasAuthenticationBuilder authenticationBuilder,
                                                final CentralAuthenticationService centralAuthenticationService) {
        super(servicesManager, ticketRegistry, validator, accessTokenFactory,
                principalFactory, webApplicationServiceServiceFactory,
                scopeToAttributesFilter, casProperties, ticketGrantingTicketCookieGenerator);
        this.refreshTokenFactory = refreshTokenFactory;
        this.accessTokenResponseGenerator = accessTokenResponseGenerator;
        this.authenticationBuilder = authenticationBuilder;
        this.centralAuthenticationService = centralAuthenticationService;
    }

    /**
     * Handle request internal model and view.
     *
     * @param request  the request
     * @param response the response
     * @throws Exception the exception
     */
    @PostMapping(path = OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.ACCESS_TOKEN_URL)
    public void handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        try {
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            if (!verifyAccessTokenRequest(request, response)) {
                LOGGER.error("Access token request verification failed");
                OAuth20Utils.writeTextError(response, OAuth20Constants.INVALID_REQUEST);
                return;
            }

            final AccessTokenRequestDataHolder responseHolder;
            try {
                responseHolder = examineAndExtractAccessTokenGrantRequest(request, response);
                LOGGER.debug("Creating access token for [{}]", responseHolder);
            } catch (final Exception e) {
                LOGGER.error("Could not identify and extract access token request", e);
                OAuth20Utils.writeTextError(response, OAuth20Constants.INVALID_GRANT);
                return;
            }

            final J2EContext context = WebUtils.getPac4jJ2EContext(request, response);
            final AccessToken accessToken = generateAccessToken(responseHolder);
            LOGGER.debug("Access token generated is: [{}]", accessToken);

            RefreshToken refreshToken = null;
            if (responseHolder.isGenerateRefreshToken()) {
                refreshToken = generateRefreshToken(responseHolder);
                LOGGER.debug("Refresh Token: [{}]", refreshToken);
            } else {
                LOGGER.debug("Service [{}] is not able/allowed to receive refresh tokens", responseHolder.getService());
            }
            generateAccessTokenResponse(request, response, responseHolder, context, accessToken, refreshToken);

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw Throwables.propagate(e);
        }
    }

    private RefreshToken generateRefreshToken(final AccessTokenRequestDataHolder responseHolder) {
        LOGGER.debug("Creating refresh token for [{}]", responseHolder.getService());
        final RefreshToken refreshToken = this.refreshTokenFactory.create(responseHolder.getService(),
                responseHolder.getAuthentication(), responseHolder.getTicketGrantingTicket());
        LOGGER.debug("Adding refresh token [{}] to the registry", refreshToken);
        addTicketToRegistry(refreshToken, responseHolder.getTicketGrantingTicket());
        return refreshToken;
    }

    private void generateAccessTokenResponse(final HttpServletRequest request, final HttpServletResponse response,
                                             final AccessTokenRequestDataHolder responseHolder,
                                             final J2EContext context, final AccessToken accessToken,
                                             final RefreshToken refreshToken) {
        LOGGER.debug("Generating access token response for [{}]", accessToken);

        final OAuth20ResponseTypes type = getOAuth20ResponseType(context);
        LOGGER.debug("Located response type as [{}]", type);

        this.accessTokenResponseGenerator.generate(request, response,
                responseHolder.getRegisteredService(),
                responseHolder.getService(),
                accessToken, refreshToken,
                casProperties.getAuthn().getOauth().getAccessToken().getMaxTimeToLiveInSeconds(), type);
    }

    private static OAuth20ResponseTypes getOAuth20ResponseType(final J2EContext context) {
        final String responseType = context.getRequestParameter(OAuth20Constants.RESPONSE_TYPE);
        final OAuth20ResponseTypes type = Arrays.stream(OAuth20ResponseTypes.values())
                .filter(t -> t.getType().equalsIgnoreCase(responseType))
                .findFirst()
                .orElse(OAuth20ResponseTypes.CODE);
        LOGGER.debug("OAuth response type is [{}]", type);
        return type;
    }

    private AccessTokenRequestDataHolder examineAndExtractAccessTokenGrantRequest(final HttpServletRequest request,
                                                                                  final HttpServletResponse response) {
        final List<BaseAccessTokenGrantRequestExtractor> list = Arrays.asList(
                new AccessTokenAuthorizationCodeGrantRequestExtractor(servicesManager, ticketRegistry,
                        request, response, centralAuthenticationService, casProperties.getAuthn().getOauth()),
                new AccessTokenRefreshTokenGrantRequestExtractor(servicesManager, ticketRegistry,
                        request, response, centralAuthenticationService, casProperties.getAuthn().getOauth()),
                new AccessTokenPasswordGrantRequestExtractor(servicesManager, ticketRegistry, request,
                        response, authenticationBuilder, centralAuthenticationService, casProperties.getAuthn().getOauth())
        );

        return list.stream()
                .filter(ext -> ext.supports(request))
                .findFirst()
                .orElseThrow((Supplier<RuntimeException>) () -> new UnsupportedOperationException("Request is not supported"))
                .extract();
    }

    /**
     * Verify the access token request.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return true, if successful
     */
    private boolean verifyAccessTokenRequest(final HttpServletRequest request, final HttpServletResponse response) {
        final String grantType = request.getParameter(OAuth20Constants.GRANT_TYPE);
        if (!isGrantTypeSupported(grantType, OAuth20GrantTypes.AUTHORIZATION_CODE, OAuth20GrantTypes.PASSWORD, OAuth20GrantTypes.REFRESH_TOKEN)) {
            LOGGER.warn("Grant type is not supported: [{}]", grantType);
            return false;
        }

        final ProfileManager manager = WebUtils.getPac4jProfileManager(request, response);
        final Optional<UserProfile> profile = manager.get(true);
        if (profile == null || !profile.isPresent()) {
            LOGGER.warn("Could not locate authenticated profile for this request");
            return false;
        }

        final UserProfile uProfile = profile.get();
        if (OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.AUTHORIZATION_CODE)) {
            final String clientId = uProfile.getId();
            final String redirectUri = request.getParameter(OAuth20Constants.REDIRECT_URI);
            final OAuthRegisteredService registeredService = OAuth20Utils.getRegisteredOAuthService(this.servicesManager, clientId);

            LOGGER.debug("Received grant type [{}] with client id [{}] and redirect URI [{}]", grantType, clientId, redirectUri);

            return uProfile instanceof OAuthClientProfile
                    && this.validator.checkParameterExist(request, OAuth20Constants.REDIRECT_URI)
                    && this.validator.checkParameterExist(request, OAuth20Constants.CODE)
                    && this.validator.checkCallbackValid(registeredService, redirectUri);

        }

        if (OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.REFRESH_TOKEN)) {
            return uProfile instanceof OAuthClientProfile && this.validator.checkParameterExist(request, OAuth20Constants.REFRESH_TOKEN);
        }

        if (OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.PASSWORD)) {
            final String clientId = request.getParameter(OAuth20Constants.CLIENT_ID);
            LOGGER.debug("Received grant type [{}] with client id [{}]", grantType, clientId);
            final OAuthRegisteredService registeredService = OAuth20Utils.getRegisteredOAuthService(this.servicesManager, clientId);

            return uProfile instanceof OAuthUserProfile
                    && this.validator.checkParameterExist(request, OAuth20Constants.CLIENT_ID)
                    && this.validator.checkServiceValid(registeredService);
        }

        return false;
    }

    /**
     * Check the grant type against expected grant types.
     *
     * @param type          the current grant type
     * @param expectedTypes the expected grant types
     * @return whether the grant type is supported
     */
    private static boolean isGrantTypeSupported(final String type, final OAuth20GrantTypes... expectedTypes) {
        LOGGER.debug("Grant type: [{}]", type);

        for (final OAuth20GrantTypes expectedType : expectedTypes) {
            if (OAuth20Utils.isGrantType(type, expectedType)) {
                return true;
            }
        }
        LOGGER.error("Unsupported grant type: [{}]", type);
        return false;
    }
}
