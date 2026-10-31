package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import module java.base;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.OAuth20Token;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link AccessTokenRefreshTokenGrantRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class AccessTokenRefreshTokenGrantRequestExtractor extends AccessTokenAuthorizationCodeGrantRequestExtractor {
    public AccessTokenRefreshTokenGrantRequestExtractor(final ObjectProvider<OAuth20ConfigurationContext> oAuthConfigurationContext) {
        super(oAuthConfigurationContext);
    }

    @Override
    public boolean supports(final HttpServletRequest request) {
        val grantType = getConfigurationContext().getObject().getRequestParameterResolver()
            .resolveRequestParameter(request, OAuth20Constants.GRANT_TYPE).orElse(StringUtils.EMPTY);
        return OAuth20Utils.isGrantType(grantType, getGrantType());
    }

    @Override
    public OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.REFRESH_TOKEN;
    }

    @Override
    protected String getOAuthParameterName() {
        return OAuth20Constants.REFRESH_TOKEN;
    }

    /**
     * The refresh token as the client sent it, decoded to its ticket id when it was issued as a JWT.
     *
     * @param request the request
     * @return the refresh token id
     */
    @Override
    protected String getOAuthParameter(final HttpServletRequest request) {
        val accessTokenJwtBuilder = getConfigurationContext().getObject().getAccessTokenJwtBuilder();
        return OAuth20JwtAccessTokenEncoder.toDecodableCipher(accessTokenJwtBuilder).decode(super.getOAuthParameter(request));
    }

    @Override
    protected AccessTokenRequestContext extractInternal(
        final HttpServletRequest request, final AccessTokenRequestContext accessTokenRequestContext) {

        val registeredService = getOAuthRegisteredServiceBy(request);
        if (registeredService == null) {
            throw UnauthorizedServiceException.denied("Unable to locate service in registry");
        }
        val shouldRenewRefreshToken = isTokenRedeemedOnce(accessTokenRequestContext.getToken(), registeredService);
        return super.extractInternal(request, accessTokenRequestContext
            .withGenerateRefreshToken(shouldRenewRefreshToken)
            .withExpireOldRefreshToken(shouldRenewRefreshToken));
    }
    
    @Override
    protected boolean isTokenRedeemedOnce(final OAuth20Token token, final OAuthRegisteredService registeredService) {
        return registeredService.isGenerateRefreshToken() && registeredService.isRenewRefreshToken();
    }

    @Override
    protected OAuthRegisteredService getOAuthRegisteredServiceBy(final HttpServletRequest request) {
        val clientId = getRegisteredServiceIdentifierFromRequest(request);
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(getConfigurationContext().getObject().getServicesManager(), clientId);
        LOGGER.debug("Located registered service [{}]", registeredService);
        return registeredService;
    }

    @Override
    protected String getRegisteredServiceIdentifierFromRequest(final HttpServletRequest request) {
        return getConfigurationContext().getObject().getRequestParameterResolver()
            .resolveClientIdAndClientSecret(request).getLeft();
    }
}
