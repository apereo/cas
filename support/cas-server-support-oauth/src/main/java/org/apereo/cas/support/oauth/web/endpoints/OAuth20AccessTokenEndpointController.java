package org.apereo.cas.support.oauth.web.endpoints;

import com.google.common.base.Throwables;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.authenticator.OAuth20CasAuthenticationBuilder;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.profile.OAuthClientProfile;
import org.apereo.cas.support.oauth.profile.OAuthUserProfile;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;
import org.apereo.cas.support.oauth.web.response.accesstoken.AccessTokenAuthorizationCodeGrantRequestExtractor;
import org.apereo.cas.support.oauth.web.response.accesstoken.AccessTokenPasswordGrantRequestExtractor;
import org.apereo.cas.support.oauth.web.response.accesstoken.AccessTokenRefreshTokenGrantRequestExtractor;
import org.apereo.cas.support.oauth.web.response.accesstoken.AccessTokenRequestDataHolder;
import org.apereo.cas.support.oauth.web.response.accesstoken.AccessTokenResponseGenerator;
import org.apereo.cas.ticket.OAuthToken;
import org.apereo.cas.ticket.TicketGrantingTicket;
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
public class OAuth20AccessTokenEndpointController extends BaseOAuth20Controller {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20AccessTokenEndpointController.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    private final RefreshTokenFactory refreshTokenFactory;
    private final AccessTokenResponseGenerator accessTokenResponseGenerator;
    private final OAuth20CasAuthenticationBuilder authenticationBuilder;

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
                                                final OAuth20CasAuthenticationBuilder authenticationBuilder) {
        super(servicesManager, ticketRegistry, validator, accessTokenFactory,
                principalFactory, webApplicationServiceServiceFactory,
                scopeToAttributesFilter, casProperties, ticketGrantingTicketCookieGenerator);
        this.refreshTokenFactory = refreshTokenFactory;
        this.accessTokenResponseGenerator = accessTokenResponseGenerator;
        this.authenticationBuilder = authenticationBuilder;
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
                return OAuth20Utils.writeTextError(response, OAuthConstants.INVALID_REQUEST);
            }
            final J2EContext context = WebUtils.getPac4jJ2EContext(request, response);
            final AccessTokenRequestDataHolder responseHolder = examineAndExtractAccessTokenRequest(request, response);
            LOGGER.debug("Creating access token for [{}]", responseHolder);

            final AccessToken accessToken = generateAccessToken(responseHolder.getService(),
                    responseHolder.getAuthentication(), context, responseHolder.getTicketGrantingTicket());

            RefreshToken refreshToken = null;
            if (responseHolder.isGenerateRefreshToken()) {
                LOGGER.debug("Creating refresh token for [{}]", responseHolder.getService());
                refreshToken = this.refreshTokenFactory.create(responseHolder.getService(),
                        responseHolder.getAuthentication(), responseHolder.getTicketGrantingTicket());
                addTicketToRegistry(refreshToken, responseHolder.getTicketGrantingTicket());
            }
            LOGGER.debug("Access token: [{}] / Timeout: [{}] (Seconds) / Refresh Token: [{}]", accessToken,
                    casProperties.getTicket().getTgt().getTimeToKillInSeconds(), refreshToken);

            final String responseType = context.getRequestParameter(OAuthConstants.RESPONSE_TYPE);
            final OAuth20ResponseTypes type = Arrays.stream(OAuth20ResponseTypes.values())
                    .filter(t -> t.getType().equalsIgnoreCase(responseType))
                    .findFirst().orElse(OAuth20ResponseTypes.CODE);
            LOGGER.debug("OAuth response type is [{}]", type);

            this.accessTokenResponseGenerator.generate(request, response,
                    responseHolder.getRegisteredService(),
                    responseHolder.getService(),
                    accessToken, refreshToken,
                    casProperties.getTicket().getTgt().getTimeToKillInSeconds(), type);

            LOGGER.debug("Adding OAuth access token [{}] to the registry", accessToken);
            addTicketToRegistry(accessToken, responseHolder.getTicketGrantingTicket());

            response.setStatus(HttpServletResponse.SC_OK);
            return null;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw Throwables.propagate(e);
        }
    }

    private AccessTokenRequestDataHolder examineAndExtractAccessTokenRequest(final HttpServletRequest request, final HttpServletResponse response) {
        AccessTokenRequestDataHolder responseHolder = null;
        if (AccessTokenAuthorizationCodeGrantRequestExtractor.supports(request)) {
            final AccessTokenAuthorizationCodeGrantRequestExtractor ext =
                    new AccessTokenAuthorizationCodeGrantRequestExtractor(servicesManager, ticketRegistry, request, response);
            responseHolder = ext.extract();
        } else if (AccessTokenRefreshTokenGrantRequestExtractor.supports(request)) {
            final AccessTokenRefreshTokenGrantRequestExtractor ext =
                    new AccessTokenRefreshTokenGrantRequestExtractor(servicesManager, ticketRegistry, request, response);
            responseHolder = ext.extract();
        } else if (AccessTokenPasswordGrantRequestExtractor.supports(request)) {
            final AccessTokenPasswordGrantRequestExtractor ext =
                    new AccessTokenPasswordGrantRequestExtractor(servicesManager, ticketRegistry, request, response, authenticationBuilder);
            responseHolder = ext.extract();
        }
        return responseHolder;
    }

    private void addTicketToRegistry(final OAuthToken ticket, final TicketGrantingTicket ticketGrantingTicket) {
        this.ticketRegistry.addTicket(ticket);
        if (ticketGrantingTicket != null) {
            this.ticketRegistry.updateTicket(ticketGrantingTicket);
        }
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
        if (OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.AUTHORIZATION_CODE)) {
            final String clientId = uProfile.getId();
            final String redirectUri = request.getParameter(OAuthConstants.REDIRECT_URI);
            final OAuthRegisteredService registeredService = OAuth20Utils.getRegisteredOAuthService(this.servicesManager, clientId);

            return uProfile instanceof OAuthClientProfile
                    && this.validator.checkParameterExist(request, OAuthConstants.REDIRECT_URI)
                    && this.validator.checkParameterExist(request, OAuthConstants.CODE)
                    && this.validator.checkCallbackValid(registeredService, redirectUri);

        } else if (OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.REFRESH_TOKEN)) {
            // refresh token grant type
            return uProfile instanceof OAuthClientProfile
                    && this.validator.checkParameterExist(request, OAuthConstants.REFRESH_TOKEN);

        } else {

            final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
            final OAuthRegisteredService registeredService = OAuth20Utils.getRegisteredOAuthService(this.servicesManager, clientId);

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
    private boolean checkGrantTypes(final String type, final OAuth20GrantTypes... expectedTypes) {
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
