package org.apereo.cas.logout.slo;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.http.HttpClient;

import lombok.val;

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

    /**
     * The default handler only applies on the CAS protocol services.
     * For the SAML and OIDC protocols, specific handlers exist.
     * For the OAuth protocol, no logout process is handled.
     *
     * The check of the service type is made by the class name not to add support-* dependencies.
     *
     * @param singleLogoutService the single logout service
     * @param registeredService   the registered service
     * @return whether the handler applies
     */
    @Override
    protected boolean supportsInternal(final WebApplicationService singleLogoutService, final RegisteredService registeredService) {
        val name = registeredService.getClass().getSimpleName();
        return !"SamlRegisteredService".equals(name) && !"OidcRegisteredService".equals(name) && !"OAuthRegisteredService".equals(name);
    }
}
