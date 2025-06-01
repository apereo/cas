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
import org.apereo.cas.ticket.OAuth20UnauthorizedScopeRequestException;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.context.WebContext;
import org.springframework.beans.factory.ObjectProvider;
import java.util.Set;
import java.util.TreeSet;

/**
 * This is {@link AccessTokenAuthorizationCodeGrantRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class AccessTokenAuthorizationCodeGrantRequestExtractor extends BaseAccessTokenGrantRequestExtractor<OAuth20ConfigurationContext> {
    public AccessTokenAuthorizationCodeGrantRequestExtractor(final ObjectProvider<OAuth20ConfigurationContext> config) {
        super(config);
    }

    protected static boolean isAllowedToGenerateRefreshToken() {
        return true;
    }

    @Override
    public AccessTokenRequestContext extractRequest(final WebContext context) throws Throwable {
        val configurationContext = getConfigurationContext().getObject();
        val grantType = configurationContext.getRequestParameterResolver()
            .resolveRequestParameter(context, OAuth20Constants.GRANT_TYPE);

        LOGGER.debug("OAuth grant type is [{}]", grantType);
        val redirectUri = getRegisteredServiceIdentifierFromRequest(context);
        val registeredService = getOAuthRegisteredServiceBy(context);
        FunctionUtils.throwIf(registeredService == null,
            () -> UnauthorizedServiceException.denied("Unable to locate service in registry for redirect URI %s ".formatted(redirectUri)));
        val requestedScopes = configurationContext.getRequestParameterResolver().resolveRequestScopes(context);
        LOGGER.debug("Requested scopes are [{}]", requestedScopes);
        val token = getOAuthTokenFromRequest(context);
        ensureTokenIsValid(token);

        val scopes = extractRequestedScopesByToken(requestedScopes, token, context);
        val service = configurationContext.getWebApplicationServiceServiceFactory().createService(redirectUri);
        val generateRefreshToken = isAllowedToGenerateRefreshToken() && registeredService.isGenerateRefreshToken();

        val builder = AccessTokenRequestContext
            .builder()
            .scopes(scopes)
            .service(service)
            .authentication(token.getAuthentication())
            .registeredService(registeredService)
            .grantType(getGrantType())
            .generateRefreshToken(generateRefreshToken)
            .token(token)
            .claims(token.getClaims())
            .ticketGrantingTicket(fetchTicketGrantingTicket(token));
        return extractInternal(context, builder.build());
    }

    @Override
    public boolean supports(final WebContext context) {
        val grantType = getConfigurationContext().getObject().getRequestParameterResolver()
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

    protected boolean ensureTokenIsValid(final OAuth20Token token) {
        val validStatefulTicket = !token.isStateless() && token.isCode()
            && getConfigurationContext().getObject().getTicketRegistry().getTicket(token.getTicketGrantingTicket().getId()) != null;
        return validStatefulTicket || (token.isStateless() && token.getAuthentication() != null && !token.isExpired());
    }

    /**
     * The requested scope MUST NOT include any scope
     * not originally granted by the resource owner, and if omitted is
     * treated as equal to the scope originally granted by the
     * resource owner.
     *
     * @param requestedScopes the requested scopes
     * @param token           the token
     * @param context         the context
     * @return scopes
     */
    protected Set<String> extractRequestedScopesByToken(final Set<String> requestedScopes,
                                                        final OAuth20Token token,
                                                        final WebContext context) {
        if (requestedScopes.isEmpty()) {
            return new TreeSet<>(token.getScopes());
        }
        if (!token.getScopes().containsAll(requestedScopes)) {
            LOGGER.error("Requested scopes [{}] exceed the granted scopes [{}] for token [{}]",
                requestedScopes, token.getScopes(), token.getId());
            throw new OAuth20UnauthorizedScopeRequestException(token.getId());
        }
        return new TreeSet<>(requestedScopes);
    }

    protected AccessTokenRequestContext extractInternal(
        final WebContext context,
        final AccessTokenRequestContext tokenRequestContext) {
        return tokenRequestContext;
    }

    protected String getRegisteredServiceIdentifierFromRequest(final WebContext context) {
        return getConfigurationContext().getObject().getRequestParameterResolver()
            .resolveRequestParameter(context, OAuth20Constants.REDIRECT_URI).orElse(StringUtils.EMPTY);
    }

    protected String getOAuthParameterName() {
        return OAuth20Constants.CODE;
    }

    protected String getOAuthParameter(final WebContext context) {
        return getConfigurationContext().getObject().getRequestParameterResolver()
            .resolveRequestParameter(context, getOAuthParameterName()).orElse(StringUtils.EMPTY);
    }

    protected OAuth20Token getOAuthTokenFromRequest(final WebContext context) {
        val id = getOAuthParameter(context);
        return getConfigurationContext().getObject().getTicketRegistry().getTicket(id, OAuth20Token.class);
    }

    protected OAuthRegisteredService getOAuthRegisteredServiceBy(final WebContext context) {
        val configurationContext = getConfigurationContext().getObject();
        val callContext = new CallContext(context, configurationContext.getSessionStore());
        val clientId = configurationContext.getRequestParameterResolver()
            .resolveClientIdAndClientSecret(callContext).getLeft();
        val redirectUri = getRegisteredServiceIdentifierFromRequest(context);
        val registeredService = StringUtils.isNotBlank(clientId)
            ? OAuth20Utils.getRegisteredOAuthServiceByClientId(configurationContext.getServicesManager(), clientId)
            : OAuth20Utils.getRegisteredOAuthServiceByRedirectUri(configurationContext.getServicesManager(), redirectUri);
        FunctionUtils.doIf(registeredService == null,
            param -> LOGGER.warn("Unable to locate registered service for clientId [{}] or redirectUri [{}]", clientId, redirectUri),
            ex -> LOGGER.debug("Located registered service [{}]", registeredService)).accept(registeredService);
        return registeredService;
    }

    protected Ticket fetchTicketGrantingTicket(final OAuth20Token token) {
        try {
            if (token.getTicketGrantingTicket() != null) {
                val id = token.getTicketGrantingTicket().getId();
                val configurationContext = getConfigurationContext().getObject();
                val ticketGrantingTicket = configurationContext.getTicketRegistry().getTicket(id, TicketGrantingTicket.class);

                FunctionUtils.doUnchecked(__ -> {
                    token.assignTicketGrantingTicket(ticketGrantingTicket);
                    configurationContext.getTicketRegistry().updateTicket(token);
                });

                return ticketGrantingTicket;
            }
        } catch (final InvalidTicketException e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }
}
