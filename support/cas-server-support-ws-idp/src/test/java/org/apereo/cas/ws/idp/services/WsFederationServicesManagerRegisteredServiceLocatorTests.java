package org.apereo.cas.ws.idp.services;

import org.apereo.cas.BaseCoreWsSecurityIdentityProviderConfigurationTests;
import org.apereo.cas.services.PartialRegexRegisteredServiceMatchingStrategy;
import org.apereo.cas.services.ServicesManagerRegisteredServiceLocator;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WsFederationServicesManagerRegisteredServiceLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WSFederation")
public class WsFederationServicesManagerRegisteredServiceLocatorTests extends BaseCoreWsSecurityIdentityProviderConfigurationTests {
    @Autowired
    @Qualifier("wsFederationServicesManagerRegisteredServiceLocator")
    private ServicesManagerRegisteredServiceLocator wsFederationServicesManagerRegisteredServiceLocator;

    @Test
    public void verifyOperation() {
        assertNotNull(wsFederationServicesManagerRegisteredServiceLocator);
        assertEquals(Ordered.HIGHEST_PRECEDENCE, wsFederationServicesManagerRegisteredServiceLocator.getOrder());
        val service = new WSFederationRegisteredService();
        service.setRealm("CAS");
        service.setServiceId("http://app.example.org/wsfed-.+");
        service.setName("WSFED App");
        service.setId(100);
        service.setAppliesTo("Example");
        service.setWsdlLocation("classpath:wsdl/ws-trust-1.4-service.wsdl");
        service.setMatchingStrategy(new PartialRegexRegisteredServiceMatchingStrategy());
        val result = wsFederationServicesManagerRegisteredServiceLocator.locate(List.of(service),
            "http://app.example.org/wsfed-whatever",
            r -> r.matches("http://app.example.org/wsfed-whatever"));
        assertNotNull(result);
    }

}
