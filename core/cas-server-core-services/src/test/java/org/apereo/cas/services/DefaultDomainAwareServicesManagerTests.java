package org.apereo.cas.services;

import org.apereo.cas.services.domain.DefaultDomainAwareServicesManager;
import org.apereo.cas.services.domain.DefaultRegisteredServiceDomainExtractor;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.NoArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Travis Schmidt
 * @since 5.2.0
 */
@NoArgsConstructor
@Tag("RegisteredService")
public class DefaultDomainAwareServicesManagerTests extends AbstractServicesManagerTests<DefaultDomainAwareServicesManager> {
    private DefaultDomainAwareServicesManager domainAwareServicesManager;

    @Test
    public void verifyOperation() {
        val input = mock(DomainAwareServicesManager.class);
        when(input.getDomains()).thenCallRealMethod();
        assertNotNull(input.getDomains());
    }

    @Test
    public void verifyDeleteEmptyDomains() {
        servicesManager.deleteAll();
        
        var r = new RegexRegisteredService();
        r.setId(10);
        r.setName("domainService1");
        r.setServiceId("https://www.example.com/one");
        servicesManager.save(r);

        assertTrue(domainAwareServicesManager.getServicesForDomain("nothing.com").isEmpty());
        assertFalse(domainAwareServicesManager.getServicesForDomain("www.example.com").isEmpty());

        r = new RegexRegisteredService();
        r.setId(20);
        r.setName("domainService2");
        r.setServiceId("https://www.example.com/two");
        servicesManager.save(r);

        assertNull(domainAwareServicesManager.findServiceBy(serviceFactory.createService("https://whatever.com")));
        assertNotNull(domainAwareServicesManager.findServiceBy(serviceFactory.createService("https://www.example.com/one")));
        
        servicesManager.deleteAll();
        assertEquals(domainAwareServicesManager.getDomains().count(), 0);
    }

    @Override
    protected ServicesManager getServicesManagerInstance() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val context = ServicesManagerConfigurationContext.builder()
            .serviceRegistry(serviceRegistry)
            .applicationContext(applicationContext)
            .environments(new HashSet<>(0))
            .servicesCache(Caffeine.newBuilder().build())
            .registeredServiceLocators(List.of(new DefaultServicesManagerRegisteredServiceLocator()))
            .build();
        domainAwareServicesManager = new DefaultDomainAwareServicesManager(context, new DefaultRegisteredServiceDomainExtractor());
        return domainAwareServicesManager;
    }
}
