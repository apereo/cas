package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.WebContext;
import org.springframework.core.Ordered;

import java.util.Objects;

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

    private final OAuth20RequestParameterResolver requestParameterResolver;

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public boolean validate(final WebContext context) {
        val responseType = requestParameterResolver.resolveRequestParameter(context, OAuth20Constants.RESPONSE_TYPE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        val grantType = requestParameterResolver.resolveRequestParameter(context, OAuth20Constants.GRANT_TYPE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        val validResponseType = OAuth20Utils.isResponseType(responseType, OAuth20ResponseTypes.DEVICE_CODE);
        val validGrantType = OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.DEVICE_CODE);

        if (!validResponseType && !validGrantType) {
            LOGGER.warn("Response type [{}] or grant type [{}] is not supported.", responseType, grantType);
            return false;
        }

        val clientId = requestParameterResolver.resolveRequestParameter(context, OAuth20Constants.CLIENT_ID).orElse(StringUtils.EMPTY);
        return FunctionUtils.doAndHandle(() -> {
            val registeredService = Objects.requireNonNull(OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, clientId));
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);
            return (validResponseType && requestParameterResolver.isAuthorizedResponseTypeForService(context, registeredService))
                   || (validGrantType && requestParameterResolver.isAuthorizedGrantTypeForService(context, registeredService));
        }, t -> {
            LOGGER.warn("Registered service access is not allowed for service definition for client id [{}]", clientId);
            return false;
        }).get();
    }

    @Override
    public boolean supports(final WebContext context) {
        val responseType = requestParameterResolver.resolveRequestParameter(context, OAuth20Constants.RESPONSE_TYPE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        val grantType = requestParameterResolver.resolveRequestParameter(context, OAuth20Constants.GRANT_TYPE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        val clientId = requestParameterResolver.resolveRequestParameter(context, OAuth20Constants.CLIENT_ID)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        val validRequest = OAuth20Utils.isResponseType(responseType, OAuth20ResponseTypes.DEVICE_CODE)
                           || OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.DEVICE_CODE);
        return validRequest && StringUtils.isNotBlank(clientId);
    }
}
