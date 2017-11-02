package org.apereo.cas.support.oauth.web.endpoints;

import com.google.common.base.Supplier;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;
import org.apereo.cas.support.oauth.web.response.accesstoken.AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.BaseAccessTokenGrantRequestExtractor;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.Pac4jUtils;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
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

    private final OAuth20TokenGenerator accessTokenGenerator;
    private final AccessTokenResponseGenerator accessTokenResponseGenerator;

    private final ExpirationPolicy accessTokenExpirationPolicy;
    private final Collection<BaseAccessTokenGrantRequestExtractor> accessTokenGrantRequestExtractors;

    public OAuth20AccessTokenEndpointController(final ServicesManager servicesManager,
                                                final TicketRegistry ticketRegistry,
                                                final OAuth20Validator validator,
                                                final AccessTokenFactory accessTokenFactory,
                                                final PrincipalFactory principalFactory,
                                                final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                                final OAuth20TokenGenerator accessTokenGenerator,
                                                final AccessTokenResponseGenerator accessTokenResponseGenerator,
                                                final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter,
                                                final CasConfigurationProperties casProperties,
                                                final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator,
                                                final ExpirationPolicy accessTokenExpirationPolicy,
                                                final Collection<BaseAccessTokenGrantRequestExtractor> accessTokenGrantRequestExtractors) {
        super(servicesManager, ticketRegistry, validator, accessTokenFactory,
                principalFactory, webApplicationServiceServiceFactory,
                scopeToAttributesFilter, casProperties, ticketGrantingTicketCookieGenerator);
        this.accessTokenGenerator = accessTokenGenerator;
        this.accessTokenResponseGenerator = accessTokenResponseGenerator;
        this.accessTokenExpirationPolicy = accessTokenExpirationPolicy;
        this.accessTokenGrantRequestExtractors = accessTokenGrantRequestExtractors;
    }

    /**
     * Handle request internal model and view.
     *
     * @param request  the request
     * @param response the response
     * @throws Exception the exception
     */
    @PostMapping(path = {OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.ACCESS_TOKEN_URL,
            OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.TOKEN_URL})
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

            final J2EContext context = Pac4jUtils.getPac4jJ2EContext(request, response);
            final Pair<AccessToken, RefreshToken> accessToken = accessTokenGenerator.generate(responseHolder);
            LOGGER.debug("Access token generated is: [{}]. Refresh token generated is [{}]", accessToken.getKey(), accessToken.getValue());
            generateAccessTokenResponse(request, response, responseHolder, context, accessToken.getKey(), accessToken.getValue());
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Handle request internal model and view.
     *
     * @param request  the request
     * @param response the response
     * @throws Exception the exception
     */
    @GetMapping(path = {OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.ACCESS_TOKEN_URL,
            OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.TOKEN_URL})
    public void handleGetRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        handleRequest(request, response);
    }


    private void generateAccessTokenResponse(final HttpServletRequest request, final HttpServletResponse response,
                                             final AccessTokenRequestDataHolder responseHolder,
                                             final J2EContext context, final AccessToken accessToken,
                                             final RefreshToken refreshToken) {
        LOGGER.debug("Generating access token response for [{}]", accessToken);

        final OAuth20ResponseTypes type = OAuth20Utils.getResponseType(context);
        LOGGER.debug("Located response type as [{}]", type);

        this.accessTokenResponseGenerator.generate(request, response,
                responseHolder.getRegisteredService(),
                responseHolder.getService(),
                accessToken, refreshToken,
                accessTokenExpirationPolicy.getTimeToLive(), type);
    }

    private AccessTokenRequestDataHolder examineAndExtractAccessTokenGrantRequest(final HttpServletRequest request,
                                                                                  final HttpServletResponse response) {
        return this.accessTokenGrantRequestExtractors.stream()
                .filter(ext -> ext.supports(request))
                .findFirst()
                .orElseThrow((Supplier<RuntimeException>) () -> new UnsupportedOperationException("Request is not supported"))
                .extract(request, response);
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
        if (!isGrantTypeSupported(grantType, OAuth20GrantTypes.values())) {
            LOGGER.warn("Grant type is not supported: [{}]", grantType);
            return false;
        }

        final ProfileManager manager = Pac4jUtils.getPac4jProfileManager(request, response);
        final Optional<UserProfile> profile = manager.get(true);
        if (profile == null || !profile.isPresent()) {
            LOGGER.warn("Could not locate authenticated profile for this request");
            return false;
        }

        final UserProfile uProfile = profile.get();
        if (uProfile == null) {
            LOGGER.warn("Could not locate authenticated profile for this request as null");
            return false;
        }
        if (OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.AUTHORIZATION_CODE)) {
            return verifyAccessForGrantAuthorizationCode(request, grantType, uProfile);
        }

        if (OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.REFRESH_TOKEN)) {
            return verifyAccessForGrantRefreshToken(request, uProfile);
        }

        if (OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.PASSWORD)) {
            return verifyAccessForGrantPassword(request, grantType, uProfile);
        }

        if (OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.CLIENT_CREDENTIALS)) {
            return verifyAccessForGrantClientCredentials(request, grantType, uProfile);
        }

        return false;
    }

    private boolean verifyAccessForGrantClientCredentials(final HttpServletRequest request, final String grantType, final UserProfile uProfile) {
        final String clientId = request.getParameter(OAuth20Constants.CLIENT_ID);
        LOGGER.debug("Received grant type [{}] with client id [{}]", grantType, clientId);
        final OAuthRegisteredService registeredService = OAuth20Utils.getRegisteredOAuthService(this.servicesManager, clientId);
        return this.validator.checkParameterExist(request, OAuth20Constants.CLIENT_ID)
                && this.validator.checkServiceValid(registeredService);
    }

    private boolean verifyAccessForGrantPassword(final HttpServletRequest request, final String grantType, final UserProfile uProfile) {
        final String clientId = request.getParameter(OAuth20Constants.CLIENT_ID);
        LOGGER.debug("Received grant type [{}] with client id [{}]", grantType, clientId);
        final OAuthRegisteredService registeredService = OAuth20Utils.getRegisteredOAuthService(this.servicesManager, clientId);

        return this.validator.checkParameterExist(request, OAuth20Constants.CLIENT_ID)
                && this.validator.checkServiceValid(registeredService);
    }

    private boolean verifyAccessForGrantRefreshToken(final HttpServletRequest request, final UserProfile uProfile) {
        if (!this.validator.checkParameterExist(request, OAuth20Constants.REFRESH_TOKEN)
                || !this.validator.checkParameterExist(request, OAuth20Constants.CLIENT_ID)
                || !this.validator.checkParameterExist(request, OAuth20Constants.CLIENT_SECRET)) {
            return false;
        }
        final String token = request.getParameter(OAuth20Constants.REFRESH_TOKEN);
        final Ticket refreshToken = ticketRegistry.getTicket(token);
        if (refreshToken == null) {
            LOGGER.warn("Provided refresh token [{}] cannot be found in the registry", token);
            return false;
        }
        if (!RefreshToken.class.isAssignableFrom(refreshToken.getClass())) {
            LOGGER.warn("Provided refresh token [{}] is found in the registry but its type is not classified as a refresh token", token);
            return false;
        }
        if (refreshToken.isExpired()) {
            LOGGER.warn("Provided refresh token [{}] has expired and is no longer valid.", token);
            return false;
        }
        return true;
    }

    private boolean verifyAccessForGrantAuthorizationCode(final HttpServletRequest request, final String grantType, final UserProfile uProfile) {
        final String clientId = uProfile.getId();
        final String redirectUri = request.getParameter(OAuth20Constants.REDIRECT_URI);
        final OAuthRegisteredService registeredService = OAuth20Utils.getRegisteredOAuthService(this.servicesManager, clientId);

        LOGGER.debug("Received grant type [{}] with client id [{}] and redirect URI [{}]", grantType, clientId, redirectUri);

        return this.validator.checkParameterExist(request, OAuth20Constants.REDIRECT_URI)
                && this.validator.checkParameterExist(request, OAuth20Constants.CODE)
                && this.validator.checkCallbackValid(registeredService, redirectUri);
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
