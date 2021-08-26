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

/**
 * @author Travis Schmidt
 * @since 5.2.0
 */
@NoArgsConstructor
@Tag("RegisteredService")
public class DefaultDomainAwareServicesManagerTests extends AbstractServicesManagerTests<DefaultDomainAwareServicesManager> {
    private DefaultDomainAwareServicesManager domainServicesManager;

    @Test
    public void verifyChangingDomain() {
        servicesManager.deleteAll();

        var r = new RegexRegisteredService();
        r.setId(1);
        r.setName("domainService1");
        r.setServiceId("https://company.com.*");
        servicesManager.save(r);
        assertFalse(domainServicesManager.getServicesForDomain("company.com").isEmpty());
        assertTrue(domainServicesManager.getServicesForDomain("default").isEmpty());

        r.setServiceId("https://company.com/.*");
        servicesManager.save(r);
        assertFalse(domainServicesManager.getServicesForDomain("company.com").isEmpty());
        assertTrue(domainServicesManager.getServicesForDomain("default").isEmpty());
        assertEquals(1, domainServicesManager.getDomains().count());

        r.setServiceId("default-.+.com");
        servicesManager.save(r);
        assertTrue(domainServicesManager.getServicesForDomain("company.com").isEmpty());
        assertFalse(domainServicesManager.getServicesForDomain("default").isEmpty());
    }

    @Test
    public void verifyDeleteEmptyDomains() {
        servicesManager.deleteAll();
        
        var r = new RegexRegisteredService();
        r.setId(10);
        r.setName("domainService1");
        r.setServiceId("https://www.example.com/one");
        servicesManager.save(r);

        assertTrue(domainServicesManager.getServicesForDomain("nothing.com").isEmpty());
        assertFalse(domainServicesManager.getServicesForDomain("www.example.com").isEmpty());

        r = new RegexRegisteredService();
        r.setId(20);
        r.setName("domainService2");
        r.setServiceId("https://www.example.com/two");
        servicesManager.save(r);

        assertNull(domainServicesManager.findServiceBy(serviceFactory.createService("https://whatever.com")));
        assertNotNull(domainServicesManager.findServiceBy(serviceFactory.createService("https://www.example.com/one")));
        
        servicesManager.deleteAll();
        assertEquals(domainServicesManager.getDomains().count(), 0);
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
        domainServicesManager = new DefaultDomainAwareServicesManager(context, new DefaultRegisteredServiceDomainExtractor());
        return domainServicesManager;
    }
}
