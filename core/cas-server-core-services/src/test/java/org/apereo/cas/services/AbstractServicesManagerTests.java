package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.util.RandomUtils;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AbstractServicesManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public abstract class AbstractServicesManagerTests<T extends ServicesManager> {
    private static final String TEST = "test";

    protected final ServiceFactory<WebApplicationService> serviceFactory = new WebApplicationServiceFactory();

    protected final List<RegisteredService> listOfDefaultServices = new ArrayList<>();

    protected ServiceRegistry serviceRegistry;

    protected ServicesManager servicesManager;

    protected AbstractServicesManagerTests() {
        val r = new RegexRegisteredService();
        r.setId(2500);
        r.setServiceId("serviceId");
        r.setName("serviceName");
        r.setEvaluationOrder(1000);
        listOfDefaultServices.add(r);
    }

    @BeforeEach
    public void initialize() {
        serviceRegistry = getServiceRegistryInstance();
        servicesManager = getServicesManagerInstance();
        servicesManager.deleteAll();
        servicesManager.load();
    }

    /**
     * Verify that a service which is <strong>not</strong> associated with the current environment ({@code current-env}
     * in this test) is not incorrectly selected due to "promotion" through the services cache. Scenario is as follows:
     * <ul>
     *      <li/> Active environment is {@code current-env}.
     *      <li/> Service {@code https://prod.test.edu} associated with {@code current-env}, low
     *      {@link RegisteredService#getEvaluationOrder()}.
     *      <li/> Service {@code https://*.test.edu} <strong>not</strong> associated with {@code current-env}, high
     *      {@link RegisteredService#getEvaluationOrder()}.
     *      <li/> {@code https://prod.test.edu} is matched correctly <em>if</em> no request for some other domain which
     *      would match {@code https://*.test.edu} is executed first.
     *      <li/> {@code https://prod.test.edu} fails to match <em>if</em> a request for some other domain which would
     *      match {@code https://*.test.edu} is executed first.
     * </ul>
     * Failure is caused by "promotion" through the services cache. When the non-{@code https://prod.test.edu} request
     * is matched, it is added to the cache <em>before</em> its associated environments were evaluated: it ends up in
     * cache even though it doesn't match the current environment. Subsequently, <em>all</em> requests matching
     * {@code https://*.test.edu} will return {@code null} because they are validated against
     * {@code https://*.test.edu}, which is not associated with {@code current-env}.
     *
     * This test verifies that the above situation does not occur. Requests for {@code https://prod.test.edu} should
     * return a valid service regardless of whether a request for {@code https://[anything else].test.edu} comes in
     * first. A service which is not associated with the current environment should not interfere.
     */
    @Test
    public void verifyInvalidServiceNotPromotedByCache() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val cache = Caffeine.newBuilder().expireAfterWrite(Duration.ofDays(1000L)).<Long, RegisteredService>build();
        val config = ServicesManagerConfigurationContext.builder()
            .serviceRegistry(serviceRegistry)
            .applicationContext(applicationContext)
            .environments(Collections.singleton("current-env"))
            .registeredServiceLocators(Collections.singletonList(new DefaultServicesManagerRegisteredServiceLocator()))
            .servicesCache(cache)
            .build();
        val manager = new DefaultServicesManager(config);

        /* Service "https://prod.test.edu", low evaluationOrder (high priority), active in "current-env". */
        val prodTestEduService = new RegexRegisteredService();
        prodTestEduService.setServiceId("https://prod\\.test\\.edu.*");
        prodTestEduService.setId(RandomUtils.nextLong());
        prodTestEduService.setName("Specific Subdomain Service");
        prodTestEduService.setEvaluationOrder(0);
        prodTestEduService.setEnvironments(new HashSet<>(Collections.singleton("current-env")));

        /* Service "https://*.test.edu", high evaluationOrder (low priority), NOT active in "current-env". */
        val anyTestEduService = new RegexRegisteredService();
        anyTestEduService.setServiceId("https://[^.]+\\.test\\.edu.*");
        anyTestEduService.setId(RandomUtils.nextLong());
        anyTestEduService.setName("Any Subdomain Service");
        anyTestEduService.setEvaluationOrder(1000);
        anyTestEduService.setEnvironments(new HashSet<>(Collections.singleton("non-current-env")));

        /* Initialize the service manager. */
        serviceRegistry.save(prodTestEduService);
        serviceRegistry.save(anyTestEduService);
        manager.load();

        /* Always worked: request for https://prod.test.edu before any request for https://[anything else].test.edu. */
        var foundService = manager.findServiceBy(serviceFactory.createService("https://prod.test.edu"));
        assertSame(prodTestEduService, foundService);

        /*
         * Now clear the cache and try the previous failure case: request for https://prod.test.edu comes after any
         * request for https://[anything else].test.edu. Previously this would fail because https://*.test.edu matches
         * https://prod.test.edu and exists in cache even though it is not associated with current-env. Null would have
         * been returned.
         */
        cache.invalidateAll();
        foundService = manager.findServiceBy(serviceFactory.createService("https://not-prod.test.edu"));
        assertNull(foundService);
        foundService = manager.findServiceBy(serviceFactory.createService("https://prod.test.edu"));
        assertSame(prodTestEduService, foundService);
    }

    @Test
    public void verifySaveAndGet() {
        val services = new RegexRegisteredService();
        services.setId(1100);
        services.setName(TEST);
        services.setServiceId(TEST);
        servicesManager.save(services);
        assertNotNull(servicesManager.findServiceBy(1100));
        assertNotNull(servicesManager.findServiceBy(1100, RegexRegisteredService.class));
        assertNotNull(servicesManager.findServiceByName(TEST));
        assertNotNull(servicesManager.findServiceByName(TEST, RegexRegisteredService.class));
        assertTrue(servicesManager.count() > 0);
        assertFalse(servicesManager.getAllServicesOfType(RegexRegisteredService.class).isEmpty());

        val mockSvc = mock(RegisteredService.class);
        assertTrue(servicesManager.getAllServicesOfType(mockSvc.getClass()).isEmpty());
    }

    @Test
    public void verifySaveInRegistryAndGetById() {
        val service = new RegexRegisteredService();
        service.setId(2100);
        service.setName(TEST);
        service.setServiceId(TEST);
        assertFalse(isServiceInCache(null, 2100));
        serviceRegistry.save(service);
        assertNotNull(serviceRegistry.findServiceById(2100));
        assertNotNull(servicesManager.findServiceBy(2100));
        assertTrue(isServiceInCache(null, 2100));
    }

    @Test
    public void verifySaveInRegistryAndGetByServiceId() {
        val service = new RegexRegisteredService();
        service.setId(3100);
        service.setName(TEST);
        service.setServiceId(TEST);
        assertFalse(isServiceInCache(TEST, 0));
        serviceRegistry.save(service);
        assertNotNull(serviceRegistry.findServiceByExactServiceId(TEST));
        val svc = new WebApplicationServiceFactory().createService(TEST);
        assertNotNull(servicesManager.findServiceBy(svc, RegexRegisteredService.class));
        assertTrue(isServiceInCache(TEST, 0));
    }

    @Test
    public void verifyDelete() {
        val r = new RegexRegisteredService();
        r.setId(1000);
        r.setName(TEST);
        r.setServiceId(TEST);
        servicesManager.save(r);
        assertTrue(isServiceInCache(null, 1000));
        assertNotNull(servicesManager.findServiceBy(serviceFactory.createService(r.getServiceId())));
        servicesManager.delete(r);
        assertNull(servicesManager.findServiceBy(r.getId()));
        assertFalse(isServiceInCache(null, 1000));
    }

    @Test
    public void verifyExpiredNotify() {
        val r = new RegexRegisteredService();
        r.setId(2000);
        r.setName(TEST);
        r.setServiceId(TEST);
        val expirationPolicy = new DefaultRegisteredServiceExpirationPolicy();
        expirationPolicy.setNotifyWhenExpired(true);
        expirationPolicy.setExpirationDate(LocalDateTime.now(ZoneOffset.UTC).minusDays(2).toString());
        r.setExpirationPolicy(expirationPolicy);
        servicesManager.save(r);
        assertNotNull(servicesManager.findServiceBy(serviceFactory.createService(r.getServiceId())));
    }

    @Test
    public void verifyExpiredNotifyAndDelete() {
        val r = new RegexRegisteredService();
        r.setId(2001);
        r.setName(TEST);
        r.setServiceId(TEST);
        val expirationPolicy = new DefaultRegisteredServiceExpirationPolicy();
        expirationPolicy.setNotifyWhenExpired(true);
        expirationPolicy.setExpirationDate(LocalDateTime.now(ZoneOffset.UTC).minusDays(2).toString());
        expirationPolicy.setDeleteWhenExpired(true);
        expirationPolicy.setNotifyWhenDeleted(true);
        r.setExpirationPolicy(expirationPolicy);
        servicesManager.save(r);
        assertNull(servicesManager.findServiceBy(serviceFactory.createService(r.getServiceId())));
    }

    /**
     * Attempts to make sure service lookup operations
     * are valid based on the existing cache, specially if load
     * takes a long time.
     *
     * @throws Exception in case threads cannot be started or joined.
     */
    @Test
    public void verifyServiceCanBeFoundDuringLoadWithoutCacheInvalidation() throws Exception {
        val service = new RegexRegisteredService();
        service.setId(RandomUtils.nextLong());
        service.setName(UUID.randomUUID().toString());
        service.setServiceId("https://test.edu.*");
        assertFalse(isServiceInCache(null, service.getId()));
        serviceRegistry.save(service);
        servicesManager.load();
        assertNotNull(servicesManager.findServiceBy(service.getId()));
        assertTrue(isServiceInCache(null, service.getId()));

        val loadingThread = new Thread(Unchecked.runnable(() -> {
            LOGGER.debug("Loading services manager...");
            Thread.sleep(1000);
            servicesManager.load();
            Thread.sleep(1000);
            LOGGER.debug("Loaded services manager...");
        }));
        loadingThread.start();

        val testService = serviceFactory.createService("https://test.edu/path/");
        IntStream.rangeClosed(1, 5).forEach(i -> {
            LOGGER.debug("Checking for previously-saved service attempt [{}]", i);
            assertNotNull(servicesManager.findServiceBy(testService));
        });
        loadingThread.join();
    }

    protected ServicesManager getServicesManagerInstance() {
        return new DefaultServicesManager(getConfigurationContext());
    }

    protected ServicesManagerConfigurationContext getConfigurationContext() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        return ServicesManagerConfigurationContext.builder()
            .serviceRegistry(serviceRegistry)
            .applicationContext(applicationContext)
            .environments(new HashSet<>(0))
            .registeredServiceLocators(List.of(new DefaultServicesManagerRegisteredServiceLocator()))
            .servicesCache(Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(2)).build())
            .build();
    }

    protected ServiceRegistry getServiceRegistryInstance() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        return new InMemoryServiceRegistry(appCtx, listOfDefaultServices, new ArrayList<>());
    }

    protected boolean isServiceInCache(final String serviceId, final long id) {
        return servicesManager.getAllServices()
            .stream()
            .anyMatch(r -> serviceId != null ? r.getServiceId().equals(serviceId) : r.getId() == id);
    }
}
