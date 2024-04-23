package org.apereo.cas.logout.slo;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.http.HttpClient;

/**
 * This is {@link DefaultSingleLogoutServiceMessageHandler} which handles the processing of logout messages
 * to logout endpoints processed by the logout manager.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultSingleLogoutServiceMessageHandler extends BaseSingleLogoutServiceMessageHandler {

    public DefaultSingleLogoutServiceMessageHandler(final HttpClient httpClient,
                                                    final SingleLogoutMessageCreator logoutMessageBuilder,
                                                    final ServicesManager servicesManager,
                                                    final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder,
                                                    final boolean asynchronous,
                                                    final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies) {
        super(httpClient, logoutMessageBuilder, servicesManager, singleLogoutServiceLogoutUrlBuilder,
            asynchronous, authenticationRequestServiceSelectionStrategies);
    }

    @Override
    protected boolean supportsInternal(final WebApplicationService singleLogoutService,
                                       final RegisteredService registeredService,
                                       final SingleLogoutExecutionRequest context) {
        return super.supportsInternal(singleLogoutService, registeredService, context)
               && registeredService.getFriendlyName().equalsIgnoreCase(CasRegisteredService.FRIENDLY_NAME);
    }
}
