package org.apereo.cas.support.oauth.validator.token;

import module java.base;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
    protected boolean validateInternal(final HttpServletRequest request, final HttpServletResponse response, final String grantType,
                                       final OAuth20UserProfile uProfile)
            throws Throwable {

        val configurationContext = getConfigurationContext().getObject();
        val clientIdAndSecret = configurationContext.getRequestParameterResolver().resolveClientIdAndClientSecret(request);
        val clientId = StringUtils.defaultIfBlank(clientIdAndSecret.getKey(), (String) uProfile.getAttribute(OAuth20Constants.CLIENT_ID));
        if (StringUtils.isBlank(clientId)) {
            LOGGER.warn("No client id is provided in the request");
            return false;
        }
        LOGGER.debug("Received grant type [{}] with client id [{}]", grantType, clientId);
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(configurationContext.getServicesManager(), clientId);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);
        val service = configurationContext.getWebApplicationServiceServiceFactory().createService(registeredService.getClientId());
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
        OAuth20Utils.verifyRequestedResources(configurationContext.getRequestParameterResolver(), request, registeredService);
        return true;
    }
}
