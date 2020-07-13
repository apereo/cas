package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.OAuth20Token;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.JEEContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Set;
import java.util.TreeSet;

/**
 * This is {@link AccessTokenAuthorizationCodeGrantRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class AccessTokenAuthorizationCodeGrantRequestExtractor extends BaseAccessTokenGrantRequestExtractor {
    public AccessTokenAuthorizationCodeGrantRequestExtractor(final OAuth20ConfigurationContext oAuthConfigurationContext) {
        super(oAuthConfigurationContext);
    }

    @Override
    public AccessTokenRequestDataHolder extract(final HttpServletRequest request, final HttpServletResponse response) {
        val context = new JEEContext(request, response, getOAuthConfigurationContext().getSessionStore());
        val grantType = request.getParameter(OAuth20Constants.GRANT_TYPE);

        LOGGER.debug("OAuth grant type is [{}]", grantType);

        val redirectUri = getRegisteredServiceIdentifierFromRequest(context);
        val registeredService = getOAuthRegisteredServiceBy(context);
        if (registeredService == null) {
            throw new UnauthorizedServiceException("Unable to locate service in registry for redirect URI " + redirectUri);
        }

        val requestedScopes = OAuth20Utils.parseRequestScopes(request);
        val token = getOAuthTokenFromRequest(request);
        if (token == null || token.isExpired()) {
            throw new InvalidTicketException(getOAuthParameter(request));
        }
        val scopes = extractRequestedScopesByToken(requestedScopes, token, request);
        val service = getOAuthConfigurationContext().getWebApplicationServiceServiceFactory().createService(redirectUri);

        val generateRefreshToken = isAllowedToGenerateRefreshToken() && registeredService.isGenerateRefreshToken();
        val builder = AccessTokenRequestDataHolder.builder()
            .scopes(scopes)
            .service(service)
            .authentication(token.getAuthentication())
            .registeredService(registeredService)
            .grantType(getGrantType())
            .generateRefreshToken(generateRefreshToken)
            .token(token)
            .claims(token.getClaims())
            .ticketGrantingTicket(token.getTicketGrantingTicket());

        return extractInternal(request, response, builder);
    }

    /**
     * Filter requested scopes by token and return final set.
     *
     * @param requestedScopes the requested scopes
     * @param token           the token
     * @param request         the request
     * @return the set
     */
    protected Set<String> extractRequestedScopesByToken(final Set<String> requestedScopes,
                                                        final OAuth20Token token, final HttpServletRequest request) {
        val scopes = new TreeSet<String>(requestedScopes);
        scopes.addAll(token.getScopes());
        return scopes;
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
     * @param context the context
     * @return the registered service identifier from request
     */
    protected String getRegisteredServiceIdentifierFromRequest(final JEEContext context) {
        return context.getRequestParameter(OAuth20Constants.REDIRECT_URI)
            .map(String::valueOf)
            .orElse(StringUtils.EMPTY);
    }

    /**
     * Is allowed to generate refresh token ?
     *
     * @return true/false
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
    protected OAuth20Token getOAuthTokenFromRequest(final HttpServletRequest request) {
        val token = getOAuthConfigurationContext().getTicketRegistry().getTicket(getOAuthParameter(request), OAuth20Token.class);
        if (token == null || token.isExpired()) {
            LOGGER.error("OAuth token indicated by parameter [{}] has expired or not found: [{}]",
                getOAuthParameter(request), token);
            if (token != null) {
                getOAuthConfigurationContext().getTicketRegistry().deleteTicket(token.getId());
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
     * Gets oauth registered service from the context.
     * Implementation attempts to locate the redirect uri from request and
     * check with service registry to find a matching oauth service.
     *
     * @param context the context
     * @return the registered service
     */
    protected OAuthRegisteredService getOAuthRegisteredServiceBy(final JEEContext context) {
        val redirectUri = getRegisteredServiceIdentifierFromRequest(context);
        var registeredService = OAuth20Utils.getRegisteredOAuthServiceByRedirectUri(
            getOAuthConfigurationContext().getServicesManager(), redirectUri);
        if (registeredService == null) {
            val clientId = OAuth20Utils.getClientIdAndClientSecret(context).getLeft();
            registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
                getOAuthConfigurationContext().getServicesManager(), clientId);
        }
        LOGGER.debug("Located registered service [{}]", registeredService);
        return registeredService;
    }
}
