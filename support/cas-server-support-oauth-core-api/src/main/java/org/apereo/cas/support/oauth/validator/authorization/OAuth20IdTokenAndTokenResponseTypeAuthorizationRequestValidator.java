package org.apereo.cas.support.oauth.validator.authorization;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;


/**
 * This is {@link OAuth20IdTokenAndTokenResponseTypeAuthorizationRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class OAuth20IdTokenAndTokenResponseTypeAuthorizationRequestValidator extends OAuth20TokenResponseTypeAuthorizationRequestValidator {
    public OAuth20IdTokenAndTokenResponseTypeAuthorizationRequestValidator(final ServicesManager servicesManager,
                                                                           final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                                                           final AuditableExecution registeredServiceAccessStrategyEnforcer) {
        super(servicesManager, webApplicationServiceServiceFactory, registeredServiceAccessStrategyEnforcer);
    }

    /**
     * Gets response type.
     *
     * @return the response type
     */
    @Override
    public OAuth20ResponseTypes getResponseType() {
        return OAuth20ResponseTypes.IDTOKEN_TOKEN;
    }
}
