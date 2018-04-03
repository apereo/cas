package org.apereo.cas.support.oauth.web.endpoints;

import com.google.common.base.Supplier;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.token.OAuth20TokenRequestValidator;
import org.apereo.cas.support.oauth.web.response.accesstoken.AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.BaseAccessTokenGrantRequestExtractor;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.Pac4jUtils;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.pac4j.core.context.J2EContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

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
@Slf4j
public class OAuth20AccessTokenEndpointController extends BaseOAuth20Controller {
    private final OAuth20TokenGenerator accessTokenGenerator;
    private final AccessTokenResponseGenerator accessTokenResponseGenerator;

    private final ExpirationPolicy accessTokenExpirationPolicy;
    private final Collection<BaseAccessTokenGrantRequestExtractor> accessTokenGrantRequestExtractors;
    private final Collection<OAuth20TokenRequestValidator> accessTokenGrantRequestValidators;

    public OAuth20AccessTokenEndpointController(final ServicesManager servicesManager,
                                                final TicketRegistry ticketRegistry,
                                                final AccessTokenFactory accessTokenFactory,
                                                final PrincipalFactory principalFactory,
                                                final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                                final OAuth20TokenGenerator accessTokenGenerator,
                                                final AccessTokenResponseGenerator accessTokenResponseGenerator,
                                                final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter,
                                                final CasConfigurationProperties casProperties,
                                                final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator,
                                                final ExpirationPolicy accessTokenExpirationPolicy,
                                                final Collection<BaseAccessTokenGrantRequestExtractor> accessTokenGrantRequestExtractors,
                                                final Collection<OAuth20TokenRequestValidator> accessTokenGrantRequestValidators) {
        super(servicesManager,
            ticketRegistry,
            accessTokenFactory,
            principalFactory,
            webApplicationServiceServiceFactory,
            scopeToAttributesFilter,
            casProperties,
            ticketGrantingTicketCookieGenerator);

        this.accessTokenGenerator = accessTokenGenerator;
        this.accessTokenResponseGenerator = accessTokenResponseGenerator;
        this.accessTokenExpirationPolicy = accessTokenExpirationPolicy;
        this.accessTokenGrantRequestExtractors = accessTokenGrantRequestExtractors;
        this.accessTokenGrantRequestValidators = accessTokenGrantRequestValidators;
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
    @SneakyThrows
    public void handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);

        try {
            if (!verifyAccessTokenRequest(request, response)) {
                throw new IllegalArgumentException("Access token validation failed");
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            OAuth20Utils.writeTextError(response, OAuth20Constants.INVALID_REQUEST);
            return;
        }

        final AccessTokenRequestDataHolder requestHolder;
        try {
            requestHolder = examineAndExtractAccessTokenGrantRequest(request, response);
            LOGGER.debug("Creating access token for [{}]", requestHolder);
        } catch (final Exception e) {
            LOGGER.error("Could not identify and extract access token request", e);
            OAuth20Utils.writeTextError(response, OAuth20Constants.INVALID_GRANT);
            return;
        }

        final J2EContext context = Pac4jUtils.getPac4jJ2EContext(request, response);
        final Pair<AccessToken, RefreshToken> accessToken = accessTokenGenerator.generate(requestHolder);
        LOGGER.debug("Access token generated is: [{}]. Refresh token generated is [{}]", accessToken.getKey(), accessToken.getValue());
        generateAccessTokenResponse(request, response, requestHolder, context, accessToken.getKey(), accessToken.getValue());
        response.setStatus(HttpServletResponse.SC_OK);
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
                                             final AccessTokenRequestDataHolder requestHolder,
                                             final J2EContext context, final AccessToken accessToken,
                                             final RefreshToken refreshToken) {
        LOGGER.debug("Generating access token response for [{}]", accessToken);
        final OAuth20ResponseTypes type = OAuth20Utils.getResponseType(context);
        LOGGER.debug("Located response type as [{}]", type);

        this.accessTokenResponseGenerator.generate(request, response,
            requestHolder.getRegisteredService(),
            requestHolder.getService(),
            accessToken,
            refreshToken,
            accessTokenExpirationPolicy.getTimeToLive(),
            type);
    }

    private AccessTokenRequestDataHolder examineAndExtractAccessTokenGrantRequest(final HttpServletRequest request,
                                                                                  final HttpServletResponse response) {
        return this.accessTokenGrantRequestExtractors.stream()
            .filter(ext -> ext.supports(request))
            .findFirst()
            .orElseThrow((Supplier<RuntimeException>) () -> new UnsupportedOperationException("Access token request is not supported"))
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
        if (accessTokenGrantRequestValidators.isEmpty()) {
            LOGGER.warn("No validators are defined to examine the access token request for eligibility");
            return false;
        }
        final J2EContext context = new J2EContext(request, response);
        return this.accessTokenGrantRequestValidators.stream()
            .filter(ext -> ext.supports(context))
            .findFirst()
            .orElseThrow((Supplier<RuntimeException>) () -> new UnsupportedOperationException("Access token request is not supported"))
            .validate(context);
    }
}
