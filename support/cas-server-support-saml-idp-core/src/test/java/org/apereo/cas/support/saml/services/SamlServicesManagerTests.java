package org.apereo.cas.support.saml.services;

import org.apereo.cas.services.AbstractServicesManagerTests;
import org.apereo.cas.services.RegisteredService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Dmitriy Kopylenko
 * @since 6.2.0
 */
public class SamlServicesManagerTests extends AbstractServicesManagerTests {

    @Override
    protected Supplier<List<RegisteredService>> registeredServicesFixture() {
        return () -> {
            var s = new SamlRegisteredService();
            s.setServiceId(".+");
            s.setName("SAML service");
            listOfDefaultServices.add(s);
            return listOfDefaultServices;
        };
    }

    @Test
    public void verifyOnlyLoadsSamlServices() {
        assertTrue(this.servicesManager.count() == 1);
    }


}
