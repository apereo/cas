package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.model.support.oauth.OAuthProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.OAuthToken;
import org.apereo.cas.ticket.registry.TicketRegistry;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link AccessTokenAuthorizationCodeGrantRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class AccessTokenAuthorizationCodeGrantRequestExtractor extends BaseAccessTokenGrantRequestExtractor {
    /**
     * Service factory instance.
     */
    protected final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory;

    public AccessTokenAuthorizationCodeGrantRequestExtractor(final ServicesManager servicesManager, final TicketRegistry ticketRegistry,
                                                             final CentralAuthenticationService centralAuthenticationService,
                                                             final OAuthProperties oAuthProperties,
                                                             final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory) {
        super(servicesManager, ticketRegistry, centralAuthenticationService, oAuthProperties);
        this.webApplicationServiceServiceFactory = webApplicationServiceServiceFactory;
    }

    @Override
    public AccessTokenRequestDataHolder extract(final HttpServletRequest request, final HttpServletResponse response) {
        val grantType = request.getParameter(OAuth20Constants.GRANT_TYPE);
        val scopes = OAuth20Utils.parseRequestScopes(request);

        LOGGER.debug("OAuth grant type is [{}]", grantType);

        val redirectUri = getRegisteredServiceIdentifierFromRequest(request);
        val registeredService = getOAuthRegisteredServiceBy(request);
        if (registeredService == null) {
            throw new UnauthorizedServiceException("Unable to locate service in registry for redirect URI " + redirectUri);
        }

        val token = getOAuthTokenFromRequest(request);
        if (token == null) {
            throw new InvalidTicketException(getOAuthParameter(request));
        }

        val service = this.webApplicationServiceServiceFactory.createService(redirectUri);
        scopes.addAll(token.getScopes());

        val generateRefreshToken = isAllowedToGenerateRefreshToken() && registeredService != null && registeredService.isGenerateRefreshToken();
        val builder = AccessTokenRequestDataHolder.builder()
            .scopes(scopes)
            .service(service)
            .authentication(token.getAuthentication())
            .registeredService(registeredService)
            .grantType(getGrantType())
            .generateRefreshToken(generateRefreshToken)
            .token(token)
            .ticketGrantingTicket(token.getTicketGrantingTicket());

        return extractInternal(request, response, builder);
    }

    /**
     * Extract internal access token request.
     *
     * @param request  the request
     * @param response the response
     * @param builder  the builder
     * @return the access token request data holder
     */
    protected AccessTokenRequestDataHolder extractInternal(final HttpServletRequest request, final HttpServletResponse response,
                                                           final AccessTokenRequestDataHolder.AccessTokenRequestDataHolderBuilder builder) {
        return builder.build();
    }

    /**
     * Gets registered service identifier from request.
     *
     * @param request the request
     * @return the registered service identifier from request
     */
    protected String getRegisteredServiceIdentifierFromRequest(final HttpServletRequest request) {
        return request.getParameter(OAuth20Constants.REDIRECT_URI);
    }

    /**
     * Is allowed to generate refresh token ?
     *
     * @return the boolean
     */
    protected boolean isAllowedToGenerateRefreshToken() {
        return true;
    }

    protected String getOAuthParameterName() {
        return OAuth20Constants.CODE;
    }

    /**
     * Gets OAuth parameter.
     *
     * @param request the request
     * @return the OAuth parameter
     */
    protected String getOAuthParameter(final HttpServletRequest request) {
        return request.getParameter(getOAuthParameterName());
    }

    /**
     * Return the OAuth token.
     *
     * @param request the request
     * @return the OAuth token
     */
    protected OAuthToken getOAuthTokenFromRequest(final HttpServletRequest request) {
        val token = this.ticketRegistry.getTicket(getOAuthParameter(request), OAuthToken.class);
        if (token == null || token.isExpired()) {
            LOGGER.error("OAuth token indicated by parameter [{}] has expired or not found: [{}]", getOAuthParameter(request), token);
            if (token != null) {
                this.ticketRegistry.deleteTicket(token.getId());
            }
            return null;
        }
        return token;
    }

    /**
     * Supports the grant type?
     *
     * @param context the context
     * @return true/false
     */
    @Override
    public boolean supports(final HttpServletRequest context) {
        val grantType = context.getParameter(OAuth20Constants.GRANT_TYPE);
        return OAuth20Utils.isGrantType(grantType, getGrantType());
    }

    @Override
    public OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.AUTHORIZATION_CODE;
    }

    @Override
    public OAuth20ResponseTypes getResponseType() {
        return OAuth20ResponseTypes.NONE;
    }

    /**
     * Gets oauth registered service from the request.
     * Implementation attempts to locate the redirect uri from request and
     * check with service registry to find a matching oauth service.
     *
     * @param request the request
     * @return the registered service
     */
    protected OAuthRegisteredService getOAuthRegisteredServiceBy(final HttpServletRequest request) {
        val redirectUri = getRegisteredServiceIdentifierFromRequest(request);
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByRedirectUri(this.servicesManager, redirectUri);
        LOGGER.debug("Located registered service [{}]", registeredService);
        return registeredService;
    }
}
