package org.apereo.cas.support.oauth.validator.authorization;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.JEEContext;

/**
 * This is {@link OAuth20MissingOrInvalidResponseTypeAuthorizationRequestValidator}.
 *
 * @author Julien Huon
 * @since 6.3.0
 */
public class OAuth20MissingOrInvalidResponseTypeAuthorizationRequestValidator extends OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator {
    public OAuth20MissingOrInvalidResponseTypeAuthorizationRequestValidator(final ServicesManager servicesManager,
                                                                            final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                                                            final AuditableExecution registeredServiceAccessStrategyEnforcer) {
        super(servicesManager, webApplicationServiceServiceFactory, registeredServiceAccessStrategyEnforcer);
    }

    @Override
    public boolean supports(final JEEContext context) {
        val responseType = context.getRequestParameter(OAuth20Constants.RESPONSE_TYPE)
            .map(String::valueOf)
            .orElse(StringUtils.EMPTY);
        return !OAuth20Utils.checkResponseTypes(responseType, OAuth20ResponseTypes.values());
    }
}
