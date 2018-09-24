package org.apereo.cas.support.saml.services.logout;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.logout.LogoutMessageCreator;
import org.apereo.cas.logout.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.slo.BaseSingleLogoutServiceMessageHandler;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.http.HttpClient;

import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link SamlIdPSingleLogoutServiceMessageHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class SamlIdPSingleLogoutServiceMessageHandler extends BaseSingleLogoutServiceMessageHandler {
    public SamlIdPSingleLogoutServiceMessageHandler(final HttpClient httpClient,
                                                    final LogoutMessageCreator logoutMessageBuilder,
                                                    final ServicesManager servicesManager,
                                                    final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder,
                                                    final boolean asynchronous,
                                                    final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies) {
        super(httpClient, logoutMessageBuilder, servicesManager,
            singleLogoutServiceLogoutUrlBuilder, asynchronous,
            authenticationRequestServiceSelectionStrategies);
    }

    @Override
    protected boolean supportsInternal(final WebApplicationService singleLogoutService, final RegisteredService registeredService) {
        return registeredService instanceof SamlRegisteredService;
    }
}
