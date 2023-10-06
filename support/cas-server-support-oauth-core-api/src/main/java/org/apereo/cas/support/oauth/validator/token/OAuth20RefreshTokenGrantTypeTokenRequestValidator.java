package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;

import java.util.Objects;

/**
 * This is {@link OAuth20RefreshTokenGrantTypeTokenRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class OAuth20RefreshTokenGrantTypeTokenRequestValidator extends BaseOAuth20TokenRequestValidator {
    public OAuth20RefreshTokenGrantTypeTokenRequestValidator(final OAuth20ConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @Override
    protected boolean validateInternal(final WebContext context, final String grantType,
                                       final ProfileManager manager, final UserProfile uProfile) throws Throwable {
        val callContext = new CallContext(context, getConfigurationContext().getSessionStore());
        val clientId = getConfigurationContext().getRequestParameterResolver()
            .resolveClientIdAndClientSecret(callContext).getLeft();
        val refreshTokenResult = getConfigurationContext().getRequestParameterResolver()
            .resolveRequestParameter(context, OAuth20Constants.REFRESH_TOKEN);
        if (refreshTokenResult.isEmpty() || clientId.isEmpty()) {
            return false;
        }

        var refreshToken = (OAuth20RefreshToken) null;
        val token = refreshTokenResult.get();
        try {
            refreshToken = getConfigurationContext().getTicketRegistry().getTicket(token, OAuth20RefreshToken.class);
            LOGGER.trace("Found valid refresh token [{}] in the registry", refreshToken);
        } catch (final InvalidTicketException e) {
            LOGGER.warn("Provided refresh token [{}] cannot be found in the registry or has expired", token);
            return false;
        }

        LOGGER.debug("Received grant type [{}] with client id [{}]", grantType, clientId);
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
            getConfigurationContext().getServicesManager(), clientId);
        val audit = AuditableContext.builder()
            .registeredService(registeredService)
            .build();
        val accessResult = getConfigurationContext().getRegisteredServiceAccessStrategyEnforcer().execute(audit);
        accessResult.throwExceptionIfNeeded();

        if (!isGrantTypeSupportedBy(registeredService, grantType)) {
            LOGGER.warn("Requested grant type [{}] is not authorized by service definition [{}]",
                grantType, Objects.requireNonNull(registeredService).getServiceId());
            return false;
        }

        if (!StringUtils.equalsIgnoreCase(refreshToken.getClientId(), clientId)) {
            LOGGER.warn("Provided refresh token [{}] does not belong to client [{}]", refreshToken.getId(), clientId);
            return false;
        }

        return true;
    }

    @Override
    protected OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.REFRESH_TOKEN;
    }
}
