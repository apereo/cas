package org.apereo.cas.support.oauth.validator.authorization;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;

import java.util.EnumSet;


/**
 * This is {@link OAuth20TokenResponseTypeAuthorizationRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class OAuth20TokenResponseTypeAuthorizationRequestValidator extends OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator {
    public OAuth20TokenResponseTypeAuthorizationRequestValidator(
        final ServicesManager servicesManager,
        final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
        final AuditableExecution registeredServiceAccessStrategyEnforcer,
        final OAuth20RequestParameterResolver requestParameterResolver) {
        super(servicesManager, webApplicationServiceServiceFactory,
            registeredServiceAccessStrategyEnforcer, requestParameterResolver);
    }

    @Override
    public EnumSet<OAuth20ResponseTypes> getSupportedResponseTypes() {
        return EnumSet.of(OAuth20ResponseTypes.TOKEN);
    }
}
