package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.JEEContext;
import org.springframework.core.Ordered;

/**
 * This is {@link OAuth20DeviceCodeResponseTypeRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
@Slf4j
@Getter
@Setter
public class OAuth20DeviceCodeResponseTypeRequestValidator implements OAuth20TokenRequestValidator {
    private final ServicesManager servicesManager;
    private final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory;

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public boolean validate(final JEEContext context) {
        val request = context.getNativeRequest();
        val responseType = request.getParameter(OAuth20Constants.RESPONSE_TYPE);
        if (!OAuth20Utils.checkResponseTypes(responseType, OAuth20ResponseTypes.values())) {
            LOGGER.warn("Response type [{}] is not supported.", responseType);
            return false;
        }

        val clientId = request.getParameter(OAuth20Constants.CLIENT_ID);
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, clientId);

        try {
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);
        } catch (final UnauthorizedServiceException e) {
            LOGGER.warn("Registered service access is not allowed for service definition for client id [{}]", clientId);
            return false;
        }
        return OAuth20Utils.isAuthorizedResponseTypeForService(context, registeredService);
    }

    @Override
    public boolean supports(final JEEContext context) {
        val responseType = context.getRequestParameter(OAuth20Constants.RESPONSE_TYPE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        val clientId = context.getRequestParameter(OAuth20Constants.CLIENT_ID)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        return OAuth20Utils.isResponseType(responseType, OAuth20ResponseTypes.DEVICE_CODE)
            && StringUtils.isNotBlank(clientId);
    }
}
