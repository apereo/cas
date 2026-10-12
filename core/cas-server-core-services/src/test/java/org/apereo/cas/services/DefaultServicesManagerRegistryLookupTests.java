package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.config.BaseAutoConfigurationTests;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.mgmt.DefaultServicesManager;
import org.apereo.cas.services.query.DefaultRegisteredServiceIndexService;
import org.apereo.cas.services.query.RegisteredServiceQuery;
import org.apereo.cas.support.events.service.CasRegisteredServiceExpiredEvent;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
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

    @Test
    void verifyFailedLoadKeepsLoadedServices() {
        val serviceRegistry = newServiceRegistry();
        val servicesManager = newServicesManager(serviceRegistry, new CasConfigurationProperties());
        val registeredService = serviceRegistry.save(newRegisteredService());
        servicesManager.load();

        doThrow(new IllegalStateException("Registry is unavailable")).when(serviceRegistry).load();
        val results = servicesManager.load();
        assertEquals(1, results.size());
        assertTrue(servicesManager.getCachedRegisteredServices().containsKey(registeredService.getId()));
        assertNotNull(servicesManager.findServiceBy(RegisteredServiceTestUtils.getService(registeredService.getServiceId())));
    }

    @Test
    void verifySaveWaitsForLoadInsteadOfDropping() throws Throwable {
        val serviceRegistry = newServiceRegistry();
        val servicesManager = newServicesManager(serviceRegistry, new CasConfigurationProperties());
        val loadStarted = new CountDownLatch(1);
        val releaseLoad = new CountDownLatch(1);
        doAnswer(invocation -> {
            loadStarted.countDown();
            assertTrue(releaseLoad.await(30, TimeUnit.SECONDS));
            return invocation.callRealMethod();
        }).when(serviceRegistry).load();

        val loader = Thread.ofVirtual().start(servicesManager::load);
        assertTrue(loadStarted.await(30, TimeUnit.SECONDS));
        val saved = CompletableFuture.supplyAsync(() -> servicesManager.save(newRegisteredService()));
        Thread.sleep(6000);
        assertFalse(saved.isDone());
        releaseLoad.countDown();
        assertNotNull(saved.get(30, TimeUnit.SECONDS));
        loader.join();
    }

    @Test
    void verifyExpiredServiceNotifiesOncePerExpiry() {
        val serviceRegistry = newServiceRegistry();
        val servicesManager = newServicesManager(serviceRegistry, new CasConfigurationProperties());
        val registeredService = newRegisteredService();
        val expirationPolicy = new DefaultRegisteredServiceExpirationPolicy();
        expirationPolicy.setNotifyWhenExpired(true);
        expirationPolicy.setExpirationDate(ZonedDateTime.now(ZoneOffset.UTC).minusDays(2).toString());
        registeredService.setExpirationPolicy(expirationPolicy);
        serviceRegistry.save(registeredService);

        val notifications = new AtomicInteger();
        configurationContext.getApplicationContext().addApplicationListener((ApplicationListener<ApplicationEvent>) event -> {
            if (event instanceof final CasRegisteredServiceExpiredEvent expired
                && expired.getRegisteredService().getId() == registeredService.getId()) {
                notifications.incrementAndGet();
            }
        });
        servicesManager.load();
        servicesManager.load();
        val service = RegisteredServiceTestUtils.getService(registeredService.getServiceId());
        assertNotNull(servicesManager.findServiceBy(service));
        assertNotNull(servicesManager.findServiceBy(service));
        assertEquals(1, notifications.get());
    }

    @Test
    void verifyExpiredServiceNotifiesAgainAfterRenewal() {
        val serviceRegistry = newServiceRegistry();
        val servicesManager = newServicesManager(serviceRegistry, new CasConfigurationProperties());
        val registeredService = newRegisteredService();
        val expirationDate = ZonedDateTime.now(ZoneOffset.UTC).minusDays(2).toString();
        val expirationPolicy = new DefaultRegisteredServiceExpirationPolicy();
        expirationPolicy.setNotifyWhenExpired(true);
        expirationPolicy.setExpirationDate(expirationDate);
        registeredService.setExpirationPolicy(expirationPolicy);
        serviceRegistry.save(registeredService);

        val notifications = new AtomicInteger();
        configurationContext.getApplicationContext().addApplicationListener((ApplicationListener<ApplicationEvent>) event -> {
            if (event instanceof final CasRegisteredServiceExpiredEvent expired
                && expired.getRegisteredService().getId() == registeredService.getId()) {
                notifications.incrementAndGet();
            }
        });
        servicesManager.load();
        expirationPolicy.setExpirationDate(ZonedDateTime.now(ZoneOffset.UTC).plusDays(2).toString());
        serviceRegistry.save(registeredService);
        servicesManager.load();
        expirationPolicy.setExpirationDate(expirationDate);
        serviceRegistry.save(registeredService);
        servicesManager.load();
        assertEquals(2, notifications.get());
    }

    @Test
    void verifyAllServicesExcludeServicesDeletedOnExpiry() {
        val serviceRegistry = newServiceRegistry();
        val servicesManager = newServicesManager(serviceRegistry, new CasConfigurationProperties());
        servicesManager.load();
        val expiredDate = ZonedDateTime.now(ZoneOffset.UTC).minusDays(2).toString();

        val deletedOnExpiry = newRegisteredService();
        val deletePolicy = new DefaultRegisteredServiceExpirationPolicy();
        deletePolicy.setDeleteWhenExpired(true);
        deletePolicy.setExpirationDate(expiredDate);
        deletedOnExpiry.setExpirationPolicy(deletePolicy);
        servicesManager.save(deletedOnExpiry);

        val notifiedOnExpiry = newRegisteredService();
        val notifyPolicy = new DefaultRegisteredServiceExpirationPolicy();
        notifyPolicy.setNotifyWhenExpired(true);
        notifyPolicy.setExpirationDate(expiredDate);
        notifiedOnExpiry.setExpirationPolicy(notifyPolicy);
        servicesManager.save(notifiedOnExpiry);

        val allServiceIds = servicesManager.getAllServices().stream().map(RegisteredService::getId).toList();
        assertFalse(allServiceIds.contains(deletedOnExpiry.getId()));
        assertTrue(allServiceIds.contains(notifiedOnExpiry.getId()));
        val servicesOfType = servicesManager.getAllServicesOfType(CasRegisteredService.class)
            .stream().map(RegisteredService::getId).toList();
        assertFalse(servicesOfType.contains(deletedOnExpiry.getId()));
    }

    @Test
    void verifyExpiredServiceIsDeletedOnLoadNotLookup() {
        val serviceRegistry = newServiceRegistry();
        val servicesManager = newServicesManager(serviceRegistry, new CasConfigurationProperties());
        servicesManager.load();
        val registeredService = newRegisteredService();
        val expirationPolicy = new DefaultRegisteredServiceExpirationPolicy();
        expirationPolicy.setDeleteWhenExpired(true);
        expirationPolicy.setExpirationDate(ZonedDateTime.now(ZoneOffset.UTC).minusDays(2).toString());
        registeredService.setExpirationPolicy(expirationPolicy);
        servicesManager.save(registeredService);

        assertNull(servicesManager.findServiceBy(RegisteredServiceTestUtils.getService(registeredService.getServiceId())));
        assertNotNull(serviceRegistry.findServiceById(registeredService.getId()));
        servicesManager.load();
        assertNull(serviceRegistry.findServiceById(registeredService.getId()));
    }

    @Test
    void verifyOversizedServiceIdIsNotMatched() {
        val casProperties = new CasConfigurationProperties();
        casProperties.getServiceRegistry().getCore().setMaximumServiceIdLength(64);
        val serviceRegistry = newServiceRegistry();
        val servicesManager = newServicesManager(serviceRegistry, casProperties);
        val registeredService = newRegisteredService();
        registeredService.setServiceId("^https://.+");
        serviceRegistry.save(registeredService);
        servicesManager.load();

        assertNotNull(servicesManager.findServiceBy(RegisteredServiceTestUtils.getService("https://app.example.org")));
        val oversized = RegisteredServiceTestUtils.getService("https://app.example.org/" + "a".repeat(64));
        assertNull(servicesManager.findServiceBy(oversized));
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
