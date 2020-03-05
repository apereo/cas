package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.OAuth20UnauthorizedScopeRequestException;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.JEEContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Set;
import java.util.TreeSet;

/**
 * This is {@link AccessTokenRefreshTokenGrantRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class AccessTokenRefreshTokenGrantRequestExtractor extends AccessTokenAuthorizationCodeGrantRequestExtractor {
    public AccessTokenRefreshTokenGrantRequestExtractor(final OAuth20ConfigurationContext oAuthConfigurationContext) {
        super(oAuthConfigurationContext);
    }

    @Override
    protected String getOAuthParameterName() {
        return OAuth20Constants.REFRESH_TOKEN;
    }

    @Override
    protected AccessTokenRequestDataHolder extractInternal(final HttpServletRequest request,
                                                           final HttpServletResponse response,
                                                           final AccessTokenRequestDataHolder.AccessTokenRequestDataHolderBuilder builder) {

        val context = new JEEContext(request, response, getOAuthConfigurationContext().getSessionStore());
        val registeredService = getOAuthRegisteredServiceBy(context);
        if (registeredService == null) {
            throw new UnauthorizedServiceException("Unable to locate service in registry ");
        }

        val shouldRenewRefreshToken = registeredService.isGenerateRefreshToken() && registeredService.isRenewRefreshToken();
        builder.generateRefreshToken(shouldRenewRefreshToken);
        builder.expireOldRefreshToken(shouldRenewRefreshToken);

        return super.extractInternal(request, response, builder);
    }

    @Override
    public boolean supports(final HttpServletRequest context) {
        val grantType = context.getParameter(OAuth20Constants.GRANT_TYPE);
        return OAuth20Utils.isGrantType(grantType, getGrantType());
    }

    @Override
    public OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.REFRESH_TOKEN;
    }

    @Override
    protected OAuthRegisteredService getOAuthRegisteredServiceBy(final JEEContext context) {
        val clientId = getRegisteredServiceIdentifierFromRequest(context);
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(getOAuthConfigurationContext().getServicesManager(), clientId);
        LOGGER.debug("Located registered service [{}]", registeredService);
        return registeredService;
    }

    @Override
    protected String getRegisteredServiceIdentifierFromRequest(final JEEContext context) {
        return OAuth20Utils.getClientIdAndClientSecret(context).getLeft();
    }

    /**
     * {@inheritDoc}
     * <p>The requested scope MUST NOT include any scope
     * not originally granted by the resource owner, and if omitted is
     * treated as equal to the scope originally granted by the
     * resource owner. </p>
     *
     * @param requestedScopes the requested scopes
     * @param token           the token
     * @param request         the request
     * @return scopes
     */
    @Override
    protected Set<String> extractRequestedScopesByToken(final Set<String> requestedScopes, final OAuth20Token token, final HttpServletRequest request) {
        if (!requestedScopes.isEmpty() && !requestedScopes.equals(token.getScopes())) {
            LOGGER.error("Requested scopes [{}} exceed the granted scopes [{}} for token [{}}", requestedScopes, token.getScopes(), token.getId());
            throw new OAuth20UnauthorizedScopeRequestException(token.getId());
        }
        return new TreeSet<>(token.getScopes());
    }
}
