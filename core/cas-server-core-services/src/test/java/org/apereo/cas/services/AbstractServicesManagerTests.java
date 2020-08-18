package org.apereo.cas.services;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AbstractServicesManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public abstract class AbstractServicesManagerTests<T extends ServicesManager> {
    private static final String TEST = "test";

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
        this.servicesManager = getServicesManagerInstance();
        this.servicesManager.load();
    }

    @Test
    public void verifySaveAndGet() {
        val services = new RegexRegisteredService();
        services.setId(1100);
        services.setName(TEST);
        services.setServiceId(TEST);
        servicesManager.save(services);
        assertNotNull(this.servicesManager.findServiceBy(1100));
        assertNotNull(this.servicesManager.findServiceBy(1100, RegexRegisteredService.class));
        assertNotNull(this.servicesManager.findServiceByName(TEST));
        assertNotNull(this.servicesManager.findServiceByName(TEST, RegexRegisteredService.class));
        assertTrue(this.servicesManager.count() > 0);
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
        assertNotNull(servicesManager.findServiceByExactServiceId(TEST));
        assertNotNull(servicesManager.findServiceBy(TEST, RegexRegisteredService.class));
        assertTrue(isServiceInCache(TEST, 0));
    }

    @Test
    public void verifyDelete() {
        val r = new RegexRegisteredService();
        r.setId(1000);
        r.setName(TEST);
        r.setServiceId(TEST);
        this.servicesManager.save(r);
        assertTrue(isServiceInCache(null, 1000));
        assertNotNull(this.servicesManager.findServiceBy(r.getServiceId()));
        this.servicesManager.delete(r);
        assertNull(this.servicesManager.findServiceBy(r.getId()));
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
        this.servicesManager.save(r);
        assertNotNull(this.servicesManager.findServiceBy(r.getServiceId()));
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
        this.servicesManager.save(r);
        assertNull(this.servicesManager.findServiceBy(r.getServiceId()));
    }

    protected ServicesManager getServicesManagerInstance() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val context = ServicesManagerConfigurationContext.builder()
            .serviceRegistry(serviceRegistry)
            .applicationContext(applicationContext)
            .environments(new HashSet<>(0))
            .servicesCache(Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(2)).build())
            .build();
        return new DefaultServicesManager(context);
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
