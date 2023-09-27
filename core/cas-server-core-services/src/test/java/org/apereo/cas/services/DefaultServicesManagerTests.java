package org.apereo.cas.services;

import org.apereo.cas.services.mgmt.DefaultServicesManager;
import org.apereo.cas.services.query.RegisteredServiceQuery;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author battags
 * @since 3.0.0
 */
@Tag("RegisteredService")
class DefaultServicesManagerTests extends AbstractServicesManagerTests<DefaultServicesManager> {

    private static final String TEST = "test";

    @Test
    void verifyFindByQuery() throws Throwable {
        val service = new CasRegisteredService();
        service.setId(1984);
        service.setName(TEST + 1984);
        service.setServiceId(service.getName());
        servicesManager.save(service);
        assertEquals(0, servicesManager.findServicesBy().count());
        assertEquals(1, servicesManager.findServicesBy(
            RegisteredServiceQuery.of(CasRegisteredService.class, "id", service.getId())).count());
        assertEquals(1, servicesManager.findServicesBy(
            RegisteredServiceQuery.of(CasRegisteredService.class, "id", service.getId()),
            RegisteredServiceQuery.of(CasRegisteredService.class, "name", service.getName())).count());
    }

    @Test
    void verifyFindByName() throws Throwable {
        val service = new CasRegisteredService();
        service.setId(6100);
        service.setName(TEST);
        service.setServiceId(TEST);

        assertNull(servicesManager.findServiceByName(null));

        serviceRegistry.save(service);
        assertNotNull(servicesManager.findServiceByName(service.getName()));
        assertNotEquals(0, servicesManager.stream().count());
        assertEquals(1, servicesManager.getDomains().count());
        assertFalse(servicesManager.getServicesForDomain(UUID.randomUUID().toString()).isEmpty());
    }

    @Test
    void verifyFindByNameAndType() throws Throwable {
        val service = new CasRegisteredService();
        service.setId(6200);
        service.setName(TEST);
        service.setServiceId(TEST);

        serviceRegistry.save(service);
        assertNotNull(servicesManager.findServiceByName(service.getName(), CasRegisteredService.class));
        assertNotEquals(0, servicesManager.stream().count());
    }

    @Test
    void verifySaveAndRemoveFromCache() throws InterruptedException {
        val service = new CasRegisteredService();
        service.setId(4000);
        service.setName(TEST);
        service.setServiceId(TEST);
        assertFalse(isServiceInCache(null, service.getId()));
        this.servicesManager.save(service);
        assertTrue(isServiceInCache(null, service.getId()));
        Thread.sleep(1_000);
        assertTrue(isServiceInCache(null, service.getId()));
        Thread.sleep(5_000);
        assertTrue(isServiceInCache(null, service.getId()));
    }

    @Test
    void verifyEmptyCacheFirst() throws Throwable {
        val service = new CasRegisteredService();
        service.setId(5000);
        service.setName(TEST);
        service.setServiceId(TEST);
        assertFalse(isServiceInCache(null, service.getId()));
        servicesManager.save(service);
        assertTrue(isServiceInCache(null, service.getId()));
        servicesManager.load();
        assertTrue(isServiceInCache(null, service.getId()));
    }
}
