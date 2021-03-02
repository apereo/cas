package org.apereo.cas.support.oauth.validator.authorization;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.JEEContext;
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

    public OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator(final ServicesManager servicesManager,
                                                                             final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                                                             final AuditableExecution registeredServiceAccessStrategyEnforcer) {
        super(servicesManager, webApplicationServiceServiceFactory, registeredServiceAccessStrategyEnforcer);

    }


    @Override
    public boolean validate(final JEEContext context) {
        val request = context.getNativeRequest();

        val authnRequest = request.getParameter(OAuth20Constants.REQUEST);
        if (StringUtils.isNotBlank(authnRequest)) {
            LOGGER.warn("Self-contained authentication requests as JWTs are not accepted");

            setErrorDetails(context,
                OAuth20Constants.REQUEST_NOT_SUPPORTED,
                StringUtils.EMPTY,
                true);

            return false;
        }

        val clientId = request.getParameter(OAuth20Constants.CLIENT_ID);
        if (!OAuth20Utils.isAuthorizedResponseTypeForService(context, getRegisteredServiceByClientId(clientId))) {
            val responseType = request.getParameter(OAuth20Constants.RESPONSE_TYPE);

            setErrorDetails(context,
                OAuth20Constants.UNAUTHORIZED_CLIENT,
                String.format("Client is not allowed to use the [%s] response_type", responseType),
                true);

            return false;
        }

        return true;
    }

    @Override
    public boolean supports(final JEEContext context) {
        if (preValidate(context)) {
            val responseType = context.getRequestParameter(OAuth20Constants.RESPONSE_TYPE);
            return OAuth20Utils.isResponseType(responseType.map(String::valueOf).orElse(StringUtils.EMPTY), getResponseType());
        }

        return false;
    }

    /**
     * Gets response type.
     *
     * @return the response type
     */
    public OAuth20ResponseTypes getResponseType() {
        return OAuth20ResponseTypes.CODE;
    }
}
