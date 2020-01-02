package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;

/**
 * This is {@link OAuth20PasswordGrantTypeTokenRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class OAuth20PasswordGrantTypeTokenRequestValidator extends BaseOAuth20TokenRequestValidator {
    public OAuth20PasswordGrantTypeTokenRequestValidator(final OAuth20ConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @Override
    protected OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.PASSWORD;
    }

    @Override
    protected boolean validateInternal(final JEEContext context, final String grantType,
                                       final ProfileManager manager, final UserProfile uProfile) {

        val clientIdAndSecret = OAuth20Utils.getClientIdAndClientSecret(context);

        if (clientIdAndSecret == null || StringUtils.isBlank(clientIdAndSecret.getKey())) {
            return false;
        }
        val clientId = clientIdAndSecret.getKey();
        LOGGER.debug("Received grant type [{}] with client id [{}]", grantType, clientId);
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(getConfigurationContext().getServicesManager(), clientId);
        val service = getConfigurationContext().getWebApplicationServiceServiceFactory().createService(registeredService.getServiceId());
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
