package org.apereo.cas.services;


import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author battags
 * @author Dmitriy Kopylenko
 * @since 3.0.0
 */
public class DefaultServicesManagerTests extends AbstractServicesManagerTests {

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
    public void verifyOnlyLoadsRegexServices() {
        assertTrue(this.servicesManager.count() == 1);
    }


}
