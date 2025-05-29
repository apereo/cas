package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.springframework.beans.factory.ObjectProvider;

/**
 * This is {@link OAuth20PasswordGrantTypeTokenRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class OAuth20PasswordGrantTypeTokenRequestValidator extends BaseOAuth20TokenRequestValidator<OAuth20ConfigurationContext> {
    public OAuth20PasswordGrantTypeTokenRequestValidator(final ObjectProvider<OAuth20ConfigurationContext> configurationContext) {
        super(configurationContext);
    }

    @Override
    protected OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.PASSWORD;
    }

    @Override
    protected boolean validateInternal(final WebContext context, final String grantType,
                                       final ProfileManager manager, final UserProfile uProfile) throws Throwable {

        val configurationContext = getConfigurationContext().getObject();
        val callContext = new CallContext(context, configurationContext.getSessionStore());
        val clientIdAndSecret = configurationContext.getRequestParameterResolver().resolveClientIdAndClientSecret(callContext);

        if (StringUtils.isBlank(clientIdAndSecret.getKey())) {
            return false;
        }
        val clientId = clientIdAndSecret.getKey();
        LOGGER.debug("Received grant type [{}] with client id [{}]", grantType, clientId);
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(configurationContext.getServicesManager(), clientId);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);
        val service = configurationContext.getWebApplicationServiceServiceFactory().createService(registeredService.getServiceId());
        val audit = AuditableContext.builder()
            .service(service)
            .registeredService(registeredService)
            .build();
        val accessResult = configurationContext.getRegisteredServiceAccessStrategyEnforcer().execute(audit);
        accessResult.throwExceptionIfNeeded();

        if (!isGrantTypeSupportedBy(registeredService, grantType)) {
            LOGGER.warn("Requested grant type [{}] is not authorized by service definition [{}]",
                grantType, registeredService.getServiceId());
            return false;
        }
        return true;
    }
}
