package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.services.ServicesManager;

/**
 * Performs an authorization check for the gateway request if there is no Ticket Granting Ticket.
 *
 * @author Scott Battaglia
 * @since 3.4.5
 */
public class GatewayServicesManagementCheckAction extends BaseServiceAuthorizationCheckAction {

    public GatewayServicesManagementCheckAction(final ServicesManager servicesManager,
                                                final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies) {
        super(servicesManager, authenticationRequestServiceSelectionStrategies);
    }
}
