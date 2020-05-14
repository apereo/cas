package org.apereo.cas.services;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ChainingServicesManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class ChainingServicesManagerTests extends AbstractServicesManagerTests {
    @Override
    protected ServicesManager getServicesManagerInstance() {
        val chain = new ChainingServicesManager();
        val manager = new DefaultServicesManager(serviceRegistry, mock(ApplicationEventPublisher.class), new HashSet<>());
        chain.registerServiceManager(manager);
        return chain;
    }

    @Test
    public void verifySupports() {
        val r = new RegexRegisteredService();
        r.setId(10);
        r.setName("domainService1");
        r.setServiceId("https://www.example.com/one");
        this.servicesManager.save(r);
        assertTrue(this.servicesManager.supports(RegexRegisteredService.class));
        assertTrue(this.servicesManager.supports(r));
        assertTrue(this.servicesManager.supports(RegisteredServiceTestUtils.getService()));
    }

    @Test
    public void verifySaveWithDomains() {
        val svc = new RegexRegisteredService();
        svc.setId(100);
        svc.setName("domainService2");
        svc.setServiceId("https://www.example.com/two");
        assertNotNull(servicesManager.save(svc, false));
        val chain = DomainAwareServicesManager.class.cast(this.servicesManager);
        assertTrue(chain.getDomains().count() == 0);
        assertTrue(chain.getServicesForDomain("example.org").isEmpty());
    }
}
