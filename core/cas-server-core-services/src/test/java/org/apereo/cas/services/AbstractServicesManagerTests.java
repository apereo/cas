package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.util.RandomUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
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
public abstract class AbstractServicesManagerTests {
    @Autowired
    @Qualifier(ServiceRegistry.BEAN_NAME)
    protected ServiceRegistry serviceRegistry;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    @Autowired
    @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
    protected ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @BeforeEach
    void initialize() {
        val registeredService = new CasRegisteredService();
        registeredService.setId(RandomUtils.nextLong());
        registeredService.setServiceId("https://app.example.org/cas");
        registeredService.setName(UUID.randomUUID().toString());
        registeredService.setEvaluationOrder(1000);

        servicesManager.deleteAll();
        servicesManager.load();
        servicesManager.save(registeredService);
    }

    @Test
    void verifySaveAndGet() {
        val registeredService = new CasRegisteredService();
        registeredService.setId(RandomUtils.nextLong());
        registeredService.setName(UUID.randomUUID().toString());
        registeredService.setServiceId(registeredService.getName());
        servicesManager.save(registeredService);
        assertNotNull(servicesManager.findServiceBy(registeredService.getId()));
        assertNotNull(servicesManager.findServiceBy(registeredService.getId(), CasRegisteredService.class));
        assertNotNull(servicesManager.findServiceByName(registeredService.getName()));
        assertNotNull(servicesManager.findServiceByName(registeredService.getName(), CasRegisteredService.class));
        assertTrue(servicesManager.count() > 0);
        assertFalse(servicesManager.getAllServicesOfType(CasRegisteredService.class).isEmpty());

        val mockSvc = mock(RegisteredService.class);
        assertTrue(servicesManager.getAllServicesOfType(mockSvc.getClass()).isEmpty());
    }

    @Test
    void verifySaveInRegistryAndGetById() {
        val registeredService = new CasRegisteredService();
        registeredService.setId(RandomUtils.nextLong());
        registeredService.setName(UUID.randomUUID().toString());
        registeredService.setServiceId(registeredService.getName());
        assertFalse(isServiceInCache(null, registeredService.getId()));
        serviceRegistry.save(registeredService);
        assertNotNull(serviceRegistry.findServiceById(registeredService.getId()));
        assertNotNull(servicesManager.findServiceBy(registeredService.getId()));
        assertTrue(isServiceInCache(null, registeredService.getId()));
    }

    @Test
    void verifySaveInRegistryAndGetByServiceId() {
        val registeredService = new CasRegisteredService();
        registeredService.setId(RandomUtils.nextLong());
        registeredService.setName(UUID.randomUUID().toString());
        registeredService.setServiceId(registeredService.getName());
        assertFalse(isServiceInCache(registeredService.getName(), 0));
        serviceRegistry.save(registeredService);
        assertNotNull(serviceRegistry.findServiceByExactServiceId(registeredService.getName()));
        val svc = webApplicationServiceFactory.createService(registeredService.getName());
        assertNotNull(servicesManager.findServiceBy(svc, CasRegisteredService.class));
        assertTrue(isServiceInCache(registeredService.getName(), 0));
    }

    @Test
    void verifyDelete() {
        val registeredService = new CasRegisteredService();
        registeredService.setId(RandomUtils.nextLong());
        registeredService.setName(UUID.randomUUID().toString());
        registeredService.setServiceId(registeredService.getName());
        servicesManager.save(registeredService);
        assertTrue(isServiceInCache(null, registeredService.getId()));
        assertNotNull(servicesManager.findServiceBy(RegisteredServiceTestUtils.getService(registeredService.getServiceId())));
        servicesManager.delete(registeredService);
        assertNull(servicesManager.findServiceBy(registeredService.getId()));
        assertFalse(isServiceInCache(null, registeredService.getId()));
    }

    @Test
    void verifyExpiredNotify() {
        val registeredService = new CasRegisteredService();
        registeredService.setId(RandomUtils.nextLong());
        registeredService.setName(UUID.randomUUID().toString());
        registeredService.setServiceId(registeredService.getName());
        val expirationPolicy = new DefaultRegisteredServiceExpirationPolicy();
        expirationPolicy.setNotifyWhenExpired(true);
        expirationPolicy.setExpirationDate(LocalDateTime.now(ZoneOffset.UTC).minusDays(2).toString());
        registeredService.setExpirationPolicy(expirationPolicy);
        servicesManager.save(registeredService);
        assertNotNull(servicesManager.findServiceBy(RegisteredServiceTestUtils.getService(registeredService.getServiceId())));
    }

    @Test
    void verifyExpiredNotifyAndDelete() {
        val registeredService = new CasRegisteredService();
        registeredService.setId(RandomUtils.nextLong());
        registeredService.setName(UUID.randomUUID().toString());
        registeredService.setServiceId(registeredService.getName());
        val expirationPolicy = new DefaultRegisteredServiceExpirationPolicy();
        expirationPolicy.setNotifyWhenExpired(true);
        expirationPolicy.setExpirationDate(LocalDateTime.now(ZoneOffset.UTC).minusDays(2).toString());
        expirationPolicy.setDeleteWhenExpired(true);
        expirationPolicy.setNotifyWhenDeleted(true);
        registeredService.setExpirationPolicy(expirationPolicy);
        servicesManager.save(registeredService);
        assertNull(servicesManager.findServiceBy(RegisteredServiceTestUtils.getService(registeredService.getServiceId())));
    }

    @Test
    void verifyReleasePolicyChainFlattened() {
        val registeredService = new CasRegisteredService();
        registeredService.setId(RandomUtils.nextLong());
        registeredService.setName(UUID.randomUUID().toString());
        registeredService.setServiceId(registeredService.getName());
        val chain = new ChainingAttributeReleasePolicy();
        val policy1 = new ReturnAllAttributeReleasePolicy();
        chain.addPolicies(policy1);
        registeredService.setAttributeReleasePolicy(chain);
        servicesManager.save(registeredService);
        val result = servicesManager.findServiceBy(RegisteredServiceTestUtils.getService(registeredService.getServiceId()));
        assertNotNull(result);
        assertInstanceOf(ReturnAllAttributeReleasePolicy.class, result.getAttributeReleasePolicy());
    }

    /**
     * Attempts to make sure service lookup operations
     * are valid based on the existing cache, specially if load
     * takes a long time.
     *
     * @throws Exception in case threads cannot be started or joined.
     */
    @Test
    void verifyServiceCanBeFoundDuringLoadWithoutCacheInvalidation() throws Throwable {
        val service = new CasRegisteredService();
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

        val testService = RegisteredServiceTestUtils.getService("https://test.edu/path/");
        IntStream.rangeClosed(1, 5).forEach(i -> {
            LOGGER.debug("Checking for previously-saved service attempt [{}]", i);
            assertNotNull(servicesManager.findServiceBy(testService));
        });
        loadingThread.join();
    }

    protected boolean isServiceInCache(final String serviceId, final long id) {
        return servicesManager.getAllServices()
            .stream()
            .anyMatch(r -> Optional.ofNullable(serviceId).map(s -> r.getServiceId().equals(s)).orElseGet(() -> r.getId() == id));
    }
}
