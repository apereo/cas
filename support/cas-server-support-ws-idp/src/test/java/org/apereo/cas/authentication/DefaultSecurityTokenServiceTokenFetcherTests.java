package org.apereo.cas.authentication;

import org.apereo.cas.BaseCoreWsSecurityIdentityProviderConfigurationTests;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultSecurityTokenServiceTokenFetcherTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("WSFederation")
public class DefaultSecurityTokenServiceTokenFetcherTests extends BaseCoreWsSecurityIdentityProviderConfigurationTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("securityTokenServiceTokenFetcher")
    private SecurityTokenServiceTokenFetcher securityTokenServiceTokenFetcher;

    @Test
    public void verifySecurityPopulator() {
        val realm = casProperties.getAuthn().getWsfedIdp().getIdp().getRealm();

        val registeredService = new WSFederationRegisteredService();
        registeredService.setRealm(realm);
        registeredService.setServiceId("http://app.example.org/wsfed-idp");
        registeredService.setName("WSFED App");
        registeredService.setId(100);
        registeredService.setAppliesTo(realm);
        registeredService.setWsdlLocation("classpath:wsdl/ws-trust-1.4-service.wsdl");
        servicesManager.save(registeredService);

        val service = RegisteredServiceTestUtils.getService("http://example.org?"
            + WSFederationConstants.WREPLY + '=' + registeredService.getServiceId() + '&'
            + WSFederationConstants.WTREALM + '=' + realm);
        service.getAttributes().put(WSFederationConstants.WREPLY, List.of(registeredService.getServiceId()));

        assertThrows(AuthenticationException.class, () -> securityTokenServiceTokenFetcher.fetch(service, "test"));
    }
}
