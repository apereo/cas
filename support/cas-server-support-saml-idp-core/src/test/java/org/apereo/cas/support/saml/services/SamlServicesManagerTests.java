package org.apereo.cas.support.saml.services;

import org.apereo.cas.services.AbstractServicesManagerTests;
import org.apereo.cas.services.ChainingServicesManager;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * @author Dmitriy Kopylenko
 * @since 6.2.0
 */
public class SamlServicesManagerTests extends AbstractServicesManagerTests {

    private SamlServicesManager samlServicesManagerUnderTest;

    @Override
    protected ServicesManager getServicesManagerInstance() {
        var chain = (ChainingServicesManager) super.getServicesManagerInstance();
        var samlSvcMgr = new SamlServicesManager(serviceRegistry, mock(ApplicationEventPublisher.class), new HashSet<>());
        chain.registerServiceManager(samlSvcMgr);
        samlServicesManagerUnderTest = samlSvcMgr;
        return chain;
    }

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
        assertEquals(1, this.samlServicesManagerUnderTest.count());
        assertEquals(SamlRegisteredService.class, this.samlServicesManagerUnderTest.getAllServices().iterator().next().getClass());
    }
}
