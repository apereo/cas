package org.apereo.cas.support.oauth.validator.token;

import module java.base;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.ObjectProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link OAuth20RefreshTokenGrantTypeTokenRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class OAuth20RefreshTokenGrantTypeTokenRequestValidator extends BaseOAuth20TokenRequestValidator<OAuth20ConfigurationContext> {
    public OAuth20RefreshTokenGrantTypeTokenRequestValidator(final ObjectProvider<OAuth20ConfigurationContext> configurationContext) {
        super(configurationContext);
    }

    @Override
    protected boolean validateInternal(final HttpServletRequest request, final HttpServletResponse response, final String grantType,
                                       final OAuth20UserProfile uProfile)
            throws Throwable {
        val configurationContext = getConfigurationContext().getObject();
        val clientId = configurationContext.getRequestParameterResolver()
            .resolveClientIdAndClientSecret(request).getLeft();
        val refreshTokenResult = configurationContext.getRequestParameterResolver()
            .resolveRequestParameter(request, OAuth20Constants.REFRESH_TOKEN);
        if (refreshTokenResult.isEmpty() || clientId.isEmpty()) {
            return false;
        }

        val token = OAuth20JwtAccessTokenEncoder.toDecodableCipher(configurationContext.getAccessTokenJwtBuilder()).decode(refreshTokenResult.get());
        val refreshToken = OAuth20Utils.getRequestedOAuthToken(request, configurationContext.getTicketRegistry(), token, OAuth20RefreshToken.class);
        if (refreshToken == null || refreshToken.isExpired()) {
            LOGGER.warn("Provided refresh token [{}] cannot be found in the registry or has expired", token);
            return false;
        }
        LOGGER.trace("Found valid refresh token [{}] in the registry", refreshToken);

        LOGGER.debug("Received grant type [{}] with client id [{}]", grantType, clientId);
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
            configurationContext.getServicesManager(), clientId);
        val audit = AuditableContext
            .builder()
            .registeredService(registeredService)
            .build();
        val accessResult = configurationContext.getRegisteredServiceAccessStrategyEnforcer().execute(audit);
        accessResult.throwExceptionIfNeeded();

        if (!isGrantTypeSupportedBy(Objects.requireNonNull(registeredService), grantType)) {
            LOGGER.warn("Requested grant type [{}] is not authorized by service definition [{}]",
                grantType, Objects.requireNonNull(registeredService).getServiceId());
            return false;
        }

        if (!Strings.CI.equals(refreshToken.getClientId(), clientId)) {
            LOGGER.warn("Provided refresh token [{}] does not belong to client [{}]", refreshToken.getId(), clientId);
            return false;
        }
        verifyBoundProofOfPossessionKey(refreshToken, OAuth20Constants.DPOP_CONFIRMATION, uProfile);
        return true;
    }

    @Override
    protected OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.REFRESH_TOKEN;
    }
}
