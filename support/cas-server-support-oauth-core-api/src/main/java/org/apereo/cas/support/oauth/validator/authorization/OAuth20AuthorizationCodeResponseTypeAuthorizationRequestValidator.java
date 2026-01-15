package org.apereo.cas.support.oauth.validator.authorization;

import module java.base;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.WebContext;
import org.springframework.core.Ordered;

/**
 * This is {@link OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
@Setter
public class OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator extends BaseOAuth20AuthorizationRequestValidator {
    private int order = Ordered.LOWEST_PRECEDENCE;

    public OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator(
        final ServicesManager servicesManager,
        final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
        final AuditableExecution registeredServiceAccessStrategyEnforcer,
        final OAuth20RequestParameterResolver requestParameterResolver) {
        super(servicesManager, webApplicationServiceServiceFactory,
            registeredServiceAccessStrategyEnforcer, requestParameterResolver);
    }

    @Override
    public boolean validate(final WebContext context) {
        val clientIdResult = requestParameterResolver.resolveRequestParameter(context, OAuth20Constants.CLIENT_ID);
        return clientIdResult
            .map(this::getRegisteredServiceByClientId)
            .filter(Objects::nonNull)
            .map(registeredService -> {
                if (!requestParameterResolver.isAuthorizedResponseTypeForService(context, registeredService)) {
                    val responseTypeResult = requestParameterResolver.resolveRequestParameter(context, OAuth20Constants.RESPONSE_TYPE);
                    val msg = String.format("Client is not allowed to use the [%s] response type", responseTypeResult.orElse("unknown"));
                    LOGGER.warn(msg);
                    setErrorDetails(context, OAuth20Constants.UNAUTHORIZED_CLIENT, msg, true);
                    return false;
                }
                return true;
            })
            .orElse(false);
    }

    @Override
    public boolean supports(final WebContext context) throws Throwable {
        if (preValidate(context)) {
            val responseType = requestParameterResolver.resolveRequestParameter(context, OAuth20Constants.RESPONSE_TYPE)
                .map(String::valueOf)
                .orElse(StringUtils.EMPTY);
            LOGGER.debug("Requested response type is [{}]", responseType);
            return getSupportedResponseTypes()
                .stream()
                .anyMatch(allowedType -> OAuth20Utils.isResponseType(responseType, allowedType));
        }
        return false;
    }

    /**
     * Gets response type.
     *
     * @return the response type
     */
    public EnumSet<OAuth20ResponseTypes> getSupportedResponseTypes() {
        return EnumSet.of(OAuth20ResponseTypes.CODE);
    }
}
