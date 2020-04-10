package org.apereo.cas.services;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ChainingWithOnlyDefaulServicesManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class ChainingWithOnlyDefaulServicesManagerTests extends AbstractServicesManagerTests {
    @Override
    protected ServicesManager getServicesManagerInstance() {
        var chain = new ChainingServicesManager();
        var manager = new DefaultServicesManager(serviceRegistry, mock(ApplicationEventPublisher.class), new HashSet<>());
        chain.registerServiceManager(manager);
        return chain;
    }

    @Test
    public void verifySupports() {
        var r = new RegexRegisteredService();
        r.setId(10);
        r.setName("domainService1");
        r.setServiceId("https://www.example.com/one");
        this.servicesManager.save(r);
        assertTrue(this.servicesManager.supports(RegexRegisteredService.class));
        assertTrue(this.servicesManager.supports(r));
        assertTrue(this.servicesManager.supports(RegisteredServiceTestUtils.getService()));
    }
}
