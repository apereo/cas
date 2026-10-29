package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import module java.base;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

    protected boolean isAllowedToGenerateRefreshToken() {
        return true;
    }

    @Override
    public AccessTokenRequestContext extractRequest(final HttpServletRequest request, final HttpServletResponse response) throws Throwable {
        val configurationContext = getConfigurationContext().getObject();
        val grantType = configurationContext.getRequestParameterResolver()
            .resolveRequestParameter(request, OAuth20Constants.GRANT_TYPE);

        LOGGER.debug("OAuth grant type is [{}]", grantType);
        val redirectUri = getRegisteredServiceIdentifierFromRequest(request);
        val registeredService = getOAuthRegisteredServiceBy(request);
        FunctionUtils.throwIf(registeredService == null,
            () -> UnauthorizedServiceException.denied("Unable to locate service in registry for redirect URI %s ".formatted(redirectUri)));
        val requestedScopes = configurationContext.getRequestParameterResolver().resolveRequestScopes(request);
        LOGGER.debug("Requested scopes are [{}]", requestedScopes);
        val token = getOAuthTokenFromRequest(request);

        val scopes = extractRequestedScopesByToken(requestedScopes, token, request);
        val resources = OAuth20Utils.narrowRequestedResources(
            configurationContext.getRequestParameterResolver().resolveRequestedResources(request), token,
            configurationContext.getRequestParameterResolver().resolveAllowedResources(registeredService));
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
            .resources(resources)
            .claims(token.getClaims())
            .ticketGrantingTicket(fetchTicketGrantingTicket(token, registeredService));
        return extractInternal(request, builder.build());
    }

    @Override
    public boolean supports(final HttpServletRequest request) {
        val grantType = getConfigurationContext().getObject().getRequestParameterResolver()
            .resolveRequestParameter(request, OAuth20Constants.GRANT_TYPE).orElse(StringUtils.EMPTY);
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

    protected AccessTokenRequestContext extractInternal(
        final HttpServletRequest request,
        final AccessTokenRequestContext tokenRequestContext) {
        return tokenRequestContext;
    }

    protected String getRegisteredServiceIdentifierFromRequest(final HttpServletRequest request) {
        return getConfigurationContext().getObject().getRequestParameterResolver()
            .resolveRequestParameter(request, OAuth20Constants.REDIRECT_URI).orElse(StringUtils.EMPTY);
    }

    protected String getOAuthParameterName() {
        return OAuth20Constants.CODE;
    }

    protected String getOAuthParameter(final HttpServletRequest request) {
        return getConfigurationContext().getObject().getRequestParameterResolver()
            .resolveRequestParameter(request, getOAuthParameterName()).orElse(StringUtils.EMPTY);
    }

    protected OAuth20Token getOAuthTokenFromRequest(final HttpServletRequest request) {
        val id = getOAuthParameter(request);
        val token = OAuth20Utils.getRequestedOAuthToken(request, getConfigurationContext().getObject().getTicketRegistry(), id, OAuth20Token.class);
        if (token == null) {
            throw new InvalidTicketException(id);
        }
        return token;
    }

    protected OAuthRegisteredService getOAuthRegisteredServiceBy(final HttpServletRequest request) {
        val configurationContext = getConfigurationContext().getObject();
        val clientId = configurationContext.getRequestParameterResolver()
            .resolveClientIdAndClientSecret(request).getLeft();
        val redirectUri = getRegisteredServiceIdentifierFromRequest(request);
        val registeredService = StringUtils.isNotBlank(clientId)
            ? OAuth20Utils.getRegisteredOAuthServiceByClientId(configurationContext.getServicesManager(), clientId)
            : OAuth20Utils.getRegisteredOAuthServiceByRedirectUri(configurationContext.getServicesManager(), redirectUri);
        FunctionUtils.doIf(registeredService == null,
            param -> LOGGER.warn("Unable to locate registered service for clientId [{}] or redirectUri [{}]", clientId, redirectUri),
            ex -> LOGGER.debug("Located registered service [{}]", registeredService)).accept(registeredService);
        return registeredService;
    }

    /**
     * Whether the token is removed once it is exchanged, as an authorization code is. Such a token is not written back
     * to the registry while the request is extracted, so that a concurrent request that already removed it does not
     * find it again.
     *
     * @param token             the token being exchanged
     * @param registeredService the registered service
     * @return true if the token is removed once exchanged
     */
    protected boolean isTokenRedeemedOnce(final OAuth20Token token, final OAuthRegisteredService registeredService) {
        return token.isCode();
    }

    protected Ticket fetchTicketGrantingTicket(final OAuth20Token token, final OAuthRegisteredService registeredService) {
        try {
            if (token.getTicketGrantingTicket() != null) {
                val id = token.getTicketGrantingTicket().getId();
                val configurationContext = getConfigurationContext().getObject();
                val ticketGrantingTicket = configurationContext.getTicketRegistry().getTicket(id, TicketGrantingTicket.class);

                FunctionUtils.doUnchecked(_ -> {
                    token.assignTicketGrantingTicket(ticketGrantingTicket);
                    if (!isTokenRedeemedOnce(token, registeredService)) {
                        configurationContext.getTicketRegistry().updateTicket(token);
                    }
                });

                return ticketGrantingTicket;
            }
        } catch (final InvalidTicketException e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }
}
