package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.util.HttpRequestUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;

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
    protected OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.REFRESH_TOKEN;
    }

    @Override
    protected boolean validateInternal(final JEEContext context, final String grantType,
                                       final ProfileManager manager, final UserProfile uProfile) {
        val request = context.getNativeRequest();
        val clientId = OAuth20Utils.getClientIdAndClientSecret(context).getLeft();
        if (!HttpRequestUtils.doesParameterExist(request, OAuth20Constants.REFRESH_TOKEN)
            || clientId.isEmpty()) {
            return false;
        }

        val token = request.getParameter(OAuth20Constants.REFRESH_TOKEN);
        val refreshToken = getConfigurationContext().getTicketRegistry().getTicket(token);
        if (refreshToken == null) {
            LOGGER.warn("Provided refresh token [{}] cannot be found in the registry", token);
            return false;
        }
        if (!OAuth20RefreshToken.class.isAssignableFrom(refreshToken.getClass())) {
            LOGGER.warn("Provided refresh token [{}] is found in the registry but its type is not classified as a refresh token", token);
            return false;
        }
        if (refreshToken.isExpired()) {
            LOGGER.warn("Provided refresh token [{}] has expired and is no longer valid.", token);
            return false;
        }

        LOGGER.debug("Received grant type [{}] with client id [{}]", grantType, clientId);
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
            getConfigurationContext().getServicesManager(), clientId);
        if (registeredService == null) {
            LOGGER.warn("Provided client id cannot be matched against a service definition");
            return false;
        }
        val service = getConfigurationContext().getWebApplicationServiceServiceFactory()
            .createService(registeredService.getServiceId());
        val audit = AuditableContext.builder()
            .service(service)
            .registeredService(registeredService)
            .build();
        val accessResult = getConfigurationContext().getRegisteredServiceAccessStrategyEnforcer().execute(audit);
        accessResult.throwExceptionIfNeeded();

        if (!isGrantTypeSupportedBy(registeredService, grantType)) {
            LOGGER.warn("Requested grant type [{}] is not authorized by service definition [{}]", getGrantType(), registeredService.getServiceId());
            return false;
        }

        return true;
    }
}
