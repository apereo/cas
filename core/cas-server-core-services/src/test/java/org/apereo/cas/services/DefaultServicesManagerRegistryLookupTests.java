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
 * Each test builds its own {@link DefaultServicesManager} over its own registry and cache,
 * so sibling tests running concurrently cannot load, save or delete what a test asserts on.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("RegisteredService")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseAutoConfigurationTests.SharedTestConfiguration.class)
class DefaultServicesManagerRegistryLookupTests {
    @Autowired
    @Qualifier(ServicesManagerConfigurationContext.BEAN_NAME)
    private ServicesManagerConfigurationContext configurationContext;

    @Test
    void verifyScheduledReloadSkipsRegistryOnMiss() {
        val serviceRegistry = newServiceRegistry();
        val servicesManager = newServicesManager(serviceRegistry, new CasConfigurationProperties());
        serviceRegistry.save(newRegisteredService());
        servicesManager.load();

        val registeredService = serviceRegistry.save(newRegisteredService());
        val service = RegisteredServiceTestUtils.getService(registeredService.getServiceId());
        assertNull(servicesManager.findServiceBy(service));
        verify(serviceRegistry, never()).findServiceBy(anyString());

        servicesManager.load();
        assertNotNull(servicesManager.findServiceBy(service));

        val unloadedService = serviceRegistry.save(newRegisteredService());
        assertNotNull(servicesManager.findServiceBy(unloadedService.getId()));
    }

    @Test
    void verifyRegistryLookupWithoutScheduledReload() {
        val casProperties = new CasConfigurationProperties();
        casProperties.getServiceRegistry().getSchedule().setEnabled(false);
        val serviceRegistry = newServiceRegistry();
        val servicesManager = newServicesManager(serviceRegistry, casProperties);
        serviceRegistry.save(newRegisteredService());
        servicesManager.load();

        val registeredService = serviceRegistry.save(newRegisteredService());
        val service = RegisteredServiceTestUtils.getService(registeredService.getServiceId());
        assertNotNull(servicesManager.findServiceBy(service));
        verify(serviceRegistry).findServiceBy(service.getId());
    }

    @Test
    void verifyPredicateLookupKeepsCacheAndIndex() {
        val serviceRegistry = newServiceRegistry();
        val servicesManager = newServicesManager(serviceRegistry, new CasConfigurationProperties());
        servicesManager.load();
        val first = servicesManager.save(newRegisteredService());
        val second = servicesManager.save(newRegisteredService());

        val results = servicesManager.findServiceBy(registeredService -> registeredService.getId() == first.getId());
        assertEquals(1, results.size());

        val cachedServices = servicesManager.getCachedRegisteredServices();
        assertTrue(cachedServices.containsKey(first.getId()));
        assertTrue(cachedServices.containsKey(second.getId()));
        assertEquals(1, servicesManager.findServicesBy(
            RegisteredServiceQuery.of(CasRegisteredService.class, "id", second.getId())).count());
        assertNotNull(servicesManager.findServiceBy(RegisteredServiceTestUtils.getService(second.getServiceId())));
    }

    private ServiceRegistry newServiceRegistry() {
        return spy(new InMemoryServiceRegistry(configurationContext.getApplicationContext()));
    }

    private CacheableServicesManager newServicesManager(final ServiceRegistry serviceRegistry,
                                                      final CasConfigurationProperties casProperties) {
        val locators = configurationContext.getRegisteredServiceLocators();
        val context = ServicesManagerConfigurationContext.builder()
            .serviceRegistry(serviceRegistry)
            .applicationContext(configurationContext.getApplicationContext())
            .servicesCache(Beans.newCacheBuilder(casProperties.getServiceRegistry().getCache()).build())
            .registeredServicesTemplatesManager(configurationContext.getRegisteredServicesTemplatesManager())
            .registeredServiceLocators(locators)
            .casProperties(casProperties)
            .tenantExtractor(configurationContext.getTenantExtractor())
            .serviceFactory(configurationContext.getServiceFactory())
            .registeredServiceIndexService(new DefaultRegisteredServiceIndexService(locators, casProperties))
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
