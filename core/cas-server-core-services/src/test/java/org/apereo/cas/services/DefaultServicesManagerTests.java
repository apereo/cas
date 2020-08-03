package org.apereo.cas.services;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author battags
 * @since 3.0.0
 */
@Tag("RegisteredService")
public class DefaultServicesManagerTests extends AbstractServicesManagerTests<DefaultServicesManager> {

    private static final String TEST = "test";

    @Test
    public void verifySaveAndRemoveFromCache() throws InterruptedException {
        val service = new RegexRegisteredService();
        service.setId(4000);
        service.setName(TEST);
        service.setServiceId(TEST);
        assertFalse(isServiceInCache(null, service.getId()));
        this.servicesManager.save(service);
        assertTrue(isServiceInCache(null, service.getId()));
        Thread.sleep(1_000);
        assertTrue(isServiceInCache(null, service.getId()));
        Thread.sleep(5_000);
        assertFalse(isServiceInCache(null, service.getId()));
    }

    @Test
    public void verifyEmptyCacheFirst() {
        val service = new RegexRegisteredService();
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
