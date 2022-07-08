package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.WebContext;

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
    public AccessTokenAuthorizationCodeGrantRequestExtractor(final OAuth20ConfigurationContext config) {
        super(config);
    }

    protected static boolean isAllowedToGenerateRefreshToken() {
        return true;
    }

    @Override
    public AccessTokenRequestContext extractRequest(final WebContext context) {
        val grantType = getConfigurationContext().getRequestParameterResolver()
            .resolveRequestParameter(context, OAuth20Constants.GRANT_TYPE);

        LOGGER.debug("OAuth grant type is [{}]", grantType);
        val redirectUri = getRegisteredServiceIdentifierFromRequest(context);
        val registeredService = getOAuthRegisteredServiceBy(context);
        FunctionUtils.throwIf(registeredService == null,
            () -> new UnauthorizedServiceException("Unable to locate service in registry for redirect URI " + redirectUri));
        val requestedScopes = getConfigurationContext().getRequestParameterResolver().resolveRequestScopes(context);
        LOGGER.debug("Requested scopes are [{}]", requestedScopes);
        val token = getOAuthTokenFromRequest(context);
        ensureTicketGrantingTicketIsNotExpired(token);

        val scopes = extractRequestedScopesByToken(requestedScopes, token, context);
        val service = getConfigurationContext().getWebApplicationServiceServiceFactory().createService(redirectUri);

        val generateRefreshToken = isAllowedToGenerateRefreshToken() && registeredService.isGenerateRefreshToken();
        val builder = AccessTokenRequestContext.builder()
            .scopes(scopes)
            .service(service)
            .authentication(token.getAuthentication())
            .registeredService(registeredService)
            .grantType(getGrantType())
            .generateRefreshToken(generateRefreshToken)
            .token(token)
            .claims(token.getClaims())
            .ticketGrantingTicket(token.getTicketGrantingTicket());
        return extractInternal(context, builder);
    }

    @Override
    public boolean supports(final WebContext context) {
        val grantType = getConfigurationContext().getRequestParameterResolver()
            .resolveRequestParameter(context, OAuth20Constants.GRANT_TYPE).orElse(StringUtils.EMPTY);
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
     * Ensure that the ticket-granting-ticket is not expired by retrieving it.
     *
     * @param token the token
     * @return the boolean
     */
    protected boolean ensureTicketGrantingTicketIsNotExpired(final OAuth20Token token) {
        return token.isCode() && getConfigurationContext().getTicketRegistry()
                                     .getTicket(token.getTicketGrantingTicket().getId()) != null;
    }

    /**
     * Filter requested scopes by token and return final set.
     *
     * @param requestedScopes the requested scopes
     * @param token           the token
     * @param context         the context
     * @return the set
     */
    protected Set<String> extractRequestedScopesByToken(final Set<String> requestedScopes,
                                                        final OAuth20Token token,
                                                        final WebContext context) {
        val scopes = new TreeSet<>(requestedScopes);
        scopes.addAll(token.getScopes());
        return scopes;
    }

    /**
     * Extract internal access token request.
     *
     * @param context the context
     * @param builder the builder
     * @return the access token request data holder
     */
    protected AccessTokenRequestContext extractInternal(
        final WebContext context,
        final AccessTokenRequestContext.AccessTokenRequestContextBuilder builder) {
        return builder.build();
    }

    /**
     * Gets registered service identifier from request.
     *
     * @param context the context
     * @return the registered service identifier from request
     */
    protected String getRegisteredServiceIdentifierFromRequest(final WebContext context) {
        return getConfigurationContext().getRequestParameterResolver()
            .resolveRequestParameter(context, OAuth20Constants.REDIRECT_URI).orElse(StringUtils.EMPTY);
    }

    protected String getOAuthParameterName() {
        return OAuth20Constants.CODE;
    }

    /**
     * Gets OAuth parameter.
     *
     * @param context the context
     * @return the OAuth parameter
     */
    protected String getOAuthParameter(final WebContext context) {
        return getConfigurationContext().getRequestParameterResolver()
            .resolveRequestParameter(context, getOAuthParameterName()).orElse(StringUtils.EMPTY);
    }

    /**
     * Return the OAuth token.
     *
     * @param context the context
     * @return the OAuth token
     */
    protected OAuth20Token getOAuthTokenFromRequest(final WebContext context) {
        val id = getOAuthParameter(context);
        return getConfigurationContext().getTicketRegistry().getTicket(id, OAuth20Token.class);
    }

    /**
     * Gets oauth registered service from the context.
     * Implementation attempts to locate the redirect uri from request and
     * check with service registry to find a matching oauth service.
     *
     * @param context the context
     * @return the registered service
     */
    protected OAuthRegisteredService getOAuthRegisteredServiceBy(final WebContext context) {
        val clientId = getConfigurationContext().getRequestParameterResolver()
            .resolveClientIdAndClientSecret(context, getConfigurationContext().getSessionStore()).getLeft();
        val redirectUri = getRegisteredServiceIdentifierFromRequest(context);
        val registeredService = StringUtils.isNotBlank(clientId)
            ? OAuth20Utils.getRegisteredOAuthServiceByClientId(getConfigurationContext().getServicesManager(), clientId)
            : OAuth20Utils.getRegisteredOAuthServiceByRedirectUri(getConfigurationContext().getServicesManager(), redirectUri);
        FunctionUtils.doIf(registeredService == null,
            param -> LOGGER.warn("Unable to locate registered service for clientId [{}] or redirectUri [{}]", clientId, redirectUri),
            ex -> LOGGER.debug("Located registered service [{}]", registeredService)).accept(registeredService);
        return registeredService;
    }
}
