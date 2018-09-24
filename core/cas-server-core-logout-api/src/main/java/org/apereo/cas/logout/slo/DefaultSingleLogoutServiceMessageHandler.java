package org.apereo.cas.logout.slo;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.logout.LogoutMessageCreator;
import org.apereo.cas.logout.SingleLogoutServiceLogoutUrlBuilder;
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
                                                    final LogoutMessageCreator logoutMessageBuilder,
                                                    final ServicesManager servicesManager,
                                                    final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder,
                                                    final boolean asynchronous,
                                                    final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies) {
        super(httpClient, logoutMessageBuilder, servicesManager, singleLogoutServiceLogoutUrlBuilder,
            asynchronous, authenticationRequestServiceSelectionStrategies);
    }
}
