package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.services.ServicesManager;

/**
 * Performs a basic check if an authentication request for a provided service is authorized to proceed
 * based on the registered services registry configuration (or lack thereof).
 *
 * @author Dmitriy Kopylenko
 * @since 3.5.1
 **/
public class ServiceAuthorizationCheckAction extends BaseServiceAuthorizationCheckAction {
    public ServiceAuthorizationCheckAction(final ServicesManager servicesManager,
                                           final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies) {
        super(servicesManager, authenticationRequestServiceSelectionStrategies);
    }
}
