package org.apereo.cas.services;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ChainingServicesManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("RegisteredService")
public class ChainingServicesManagerTests extends AbstractServicesManagerTests<ChainingServicesManager> {
    @Test
    public void verifySupports() {
        val r = new RegexRegisteredService();
        r.setId(10);
        r.setName("domainService1");
        r.setServiceId("https://www.example.com/one");
        servicesManager.save(r);
        assertTrue(servicesManager.supports(RegexRegisteredService.class));
        assertTrue(servicesManager.supports(r));
        assertTrue(servicesManager.supports(RegisteredServiceTestUtils.getService()));
    }

    @Test
    public void verifySaveWithDomains() {
        val svc = new RegexRegisteredService();
        svc.setId(100);
        svc.setName("domainService2");
        svc.setServiceId("https://www.example.com/two");
        assertNotNull(servicesManager.save(svc, false));
        val chain = DomainAwareServicesManager.class.cast(servicesManager);
        assertTrue(chain.getDomains().count() == 0);
        assertTrue(chain.getServicesForDomain("example.org").isEmpty());
    }

    @Override
    protected ServicesManager getServicesManagerInstance() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val chain = new ChainingServicesManager();
        val context = ServicesManagerConfigurationContext.builder()
            .serviceRegistry(serviceRegistry)
            .applicationContext(applicationContext)
            .environments(new HashSet<>())
            .servicesCache(Caffeine.newBuilder().build())
            .build();
        val manager = new DefaultServicesManager(context);
        chain.registerServiceManager(manager);
        return chain;
    }
}
