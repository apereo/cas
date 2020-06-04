package org.apereo.cas.services;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author battags
 * @since 3.0.0
 */
@Tag("Simple")
public class DefaultServicesManagerTests extends AbstractServicesManagerTests {

    private static final String TEST = "test";

    @Test
    public void verifySaveAndRemoveFromCache() throws InterruptedException {
        val service = new RegexRegisteredService();
        service.setId(4100);
        service.setName(TEST);
        service.setServiceId(TEST);
        assertFalse(isServiceInCache(null, 4100));
        this.servicesManager.save(service);
        assertTrue(isServiceInCache(null, 4100));
        Thread.sleep(1_000);
        assertTrue(isServiceInCache(null, 4100));
        Thread.sleep(5_000);
        assertFalse(isServiceInCache(null, 4100));
    }
}
