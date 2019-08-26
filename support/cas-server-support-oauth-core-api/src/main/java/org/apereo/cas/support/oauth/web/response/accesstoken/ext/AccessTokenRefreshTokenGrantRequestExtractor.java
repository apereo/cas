package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    protected AccessTokenRequestDataHolder extractInternal(HttpServletRequest request,
        HttpServletResponse response,
        AccessTokenRequestDataHolder.AccessTokenRequestDataHolderBuilder builder) {
        val registeredService = getOAuthRegisteredServiceBy(request);
        if (registeredService == null) {
            throw new UnauthorizedServiceException("Unable to locate service in registry ");
        }

        boolean shouldRenewRefreshToken =
            registeredService.isGenerateRefreshToken() && registeredService.isRenewRefreshToken();
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
    protected OAuthRegisteredService getOAuthRegisteredServiceBy(final HttpServletRequest request) {
        val clientId = getRegisteredServiceIdentifierFromRequest(request);
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(getOAuthConfigurationContext().getServicesManager(), clientId);
        LOGGER.debug("Located registered service [{}]", registeredService);
        return registeredService;
    }

    @Override
    protected String getRegisteredServiceIdentifierFromRequest(final HttpServletRequest request) {
        return request.getParameter(OAuth20Constants.CLIENT_ID);
    }
}
