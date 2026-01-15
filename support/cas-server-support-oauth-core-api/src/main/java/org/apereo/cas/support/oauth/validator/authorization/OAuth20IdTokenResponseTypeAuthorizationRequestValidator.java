package org.apereo.cas.support.oauth.validator.authorization;

import module java.base;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;


/**
 * This is {@link OAuth20IdTokenResponseTypeAuthorizationRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class OAuth20IdTokenResponseTypeAuthorizationRequestValidator extends OAuth20TokenResponseTypeAuthorizationRequestValidator {
    public OAuth20IdTokenResponseTypeAuthorizationRequestValidator(
        final ServicesManager servicesManager,
        final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
        final AuditableExecution registeredServiceAccessStrategyEnforcer,
        final OAuth20RequestParameterResolver requestParameterResolver) {
        super(servicesManager, webApplicationServiceServiceFactory,
            registeredServiceAccessStrategyEnforcer, requestParameterResolver);
    }

    @Override
    public EnumSet<OAuth20ResponseTypes> getSupportedResponseTypes() {
        return EnumSet.of(OAuth20ResponseTypes.ID_TOKEN);
    }
}
