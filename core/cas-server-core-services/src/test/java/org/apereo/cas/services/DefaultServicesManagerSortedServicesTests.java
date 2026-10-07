package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.config.BaseAutoConfigurationTests;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.mgmt.DefaultServicesManager;
import org.apereo.cas.services.query.DefaultRegisteredServiceIndexService;
import org.apereo.cas.services.query.RegisteredServiceQuery;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.RandomUtils;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Each test builds its own {@link DefaultServicesManager} over its own registry, cache and index,
 * so sibling tests running concurrently cannot change what a test asserts on.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("RegisteredService")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseAutoConfigurationTests.SharedTestConfiguration.class)
class DefaultServicesManagerSortedServicesTests {
    private static final String DOMAIN = "example.org";

    @Autowired
    @Qualifier(ServicesManagerConfigurationContext.BEAN_NAME)
    private ServicesManagerConfigurationContext configurationContext;

    @Test
    void verifySortedServicesReusedUntilCacheChanges() {
        val casProperties = new CasConfigurationProperties();
        val servicesManager = newServicesManager(newServiceRegistry(), newServicesCache(casProperties),
            newIndexService(casProperties), casProperties);
        servicesManager.save(newRegisteredService());
        servicesManager.save(newRegisteredService());

        val sortedServices = servicesManager.getServicesForDomain(DOMAIN);
        assertEquals(2, sortedServices.size());
        assertSame(sortedServices, servicesManager.getServicesForDomain(DOMAIN));
        servicesManager.getAllServices();
        servicesManager.getAllServicesOfType(CasRegisteredService.class);
        assertSame(sortedServices, servicesManager.getServicesForDomain(DOMAIN));

        val addedService = servicesManager.save(newRegisteredService());
        val afterSave = servicesManager.getServicesForDomain(DOMAIN);
        assertNotSame(sortedServices, afterSave);
        assertTrue(afterSave.contains(addedService));

        servicesManager.delete(addedService);
        assertFalse(servicesManager.getServicesForDomain(DOMAIN).contains(addedService));
    }

    @Test
    void verifyServicesRemovedBehindManagerAreDropped() {
        val casProperties = new CasConfigurationProperties();
        val servicesCache = newServicesCache(casProperties);
        val servicesManager = newServicesManager(newServiceRegistry(), servicesCache,
            newIndexService(casProperties), casProperties);
        val evictedService = servicesManager.save(newRegisteredService());
        val keptService = servicesManager.save(newRegisteredService());
        assertEquals(2, servicesManager.getServicesForDomain(DOMAIN).size());

        servicesCache.invalidate(evictedService.getId());
        val sortedServices = servicesManager.getServicesForDomain(DOMAIN);
        assertEquals(1, sortedServices.size());
        assertTrue(sortedServices.contains(keptService));
        assertNull(servicesManager.findServiceBy(RegisteredServiceTestUtils.getService(evictedService.getServiceId())));
        assertNotNull(servicesManager.findServiceBy(RegisteredServiceTestUtils.getService(keptService.getServiceId())));
    }

    @Test
    void verifyReadsDoNotReindexCachedServices() {
        val casProperties = new CasConfigurationProperties();
        val indexService = spy(newIndexService(casProperties));
        val servicesManager = newServicesManager(newServiceRegistry(), newServicesCache(casProperties), indexService, casProperties);
        servicesManager.save(newRegisteredService());
        servicesManager.save(newRegisteredService());
        clearInvocations(indexService);

        servicesManager.getAllServices();
        servicesManager.getAllServicesOfType(CasRegisteredService.class);
        verify(indexService, never()).indexService(any());
        assertEquals(2, servicesManager.countIndexedServices());
    }

    @Test
    void verifySortedServicesReusedWithCacheDisabled() {
        val casProperties = new CasConfigurationProperties();
        casProperties.getServiceRegistry().getCache().setCacheSize(0);
        casProperties.getServiceRegistry().getSchedule().setEnabled(false);
        val serviceRegistry = newServiceRegistry();
        val servicesManager = newServicesManager(serviceRegistry, newServicesCache(casProperties),
            newIndexService(casProperties), casProperties);
        val registeredService = serviceRegistry.save(newRegisteredService());
        servicesManager.load();

        val service = RegisteredServiceTestUtils.getService(registeredService.getServiceId());
        assertNotNull(servicesManager.findServiceBy(service));
        clearInvocations(serviceRegistry);
        assertNotNull(servicesManager.findServiceBy(service));
        verify(serviceRegistry, never()).getServicesStream();
    }

    @Test
    void verifyIndexReplacesServiceById() {
        val indexService = newIndexService(new CasConfigurationProperties());
        indexService.initialize();
        val originalService = newRegisteredService();
        indexService.indexService(originalService);
        val updatedService = newRegisteredService();
        updatedService.setId(originalService.getId());
        indexService.indexService(updatedService);

        assertEquals(1, indexService.count());
        assertSame(updatedService, indexService.findServiceBy(originalService.getId()).orElseThrow());
        assertEquals(1, indexService.findServiceBy(
            RegisteredServiceQuery.of(CasRegisteredService.class, "name", updatedService.getName())).count());
        assertEquals(0, indexService.findServiceBy(
            RegisteredServiceQuery.of(CasRegisteredService.class, "name", originalService.getName())).count());
    }

    private ServiceRegistry newServiceRegistry() {
        return spy(new InMemoryServiceRegistry(configurationContext.getApplicationContext()));
    }

    private DefaultRegisteredServiceIndexService newIndexService(final CasConfigurationProperties casProperties) {
        return new DefaultRegisteredServiceIndexService(configurationContext.getRegisteredServiceLocators(), casProperties);
    }

    private static Cache<Long, RegisteredService> newServicesCache(final CasConfigurationProperties casProperties) {
        return Beans.newCacheBuilder(casProperties.getServiceRegistry().getCache()).build();
    }

    private DefaultServicesManager newServicesManager(final ServiceRegistry serviceRegistry,
                                                      final Cache<Long, RegisteredService> servicesCache,
                                                      final RegisteredServiceIndexService indexService,
                                                      final CasConfigurationProperties casProperties) {
        val context = ServicesManagerConfigurationContext.builder()
            .serviceRegistry(serviceRegistry)
            .applicationContext(configurationContext.getApplicationContext())
            .servicesCache(servicesCache)
            .registeredServicesTemplatesManager(configurationContext.getRegisteredServicesTemplatesManager())
            .registeredServiceLocators(configurationContext.getRegisteredServiceLocators())
            .casProperties(casProperties)
            .tenantExtractor(configurationContext.getTenantExtractor())
            .serviceFactory(configurationContext.getServiceFactory())
            .registeredServiceIndexService(indexService)
            .build();
        return new DefaultServicesManager(context);
    }

    private static CasRegisteredService newRegisteredService() {
        val registeredService = new CasRegisteredService();
        registeredService.setId(RandomUtils.nextLong());
        registeredService.setName(UUID.randomUUID().toString());
        registeredService.setServiceId("https://%s.example.org/app".formatted(registeredService.getName()));
        return registeredService;
    }
}
