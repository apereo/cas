package org.apereo.cas.cache;

import org.apereo.cas.util.cache.DistributedCacheManager;
import org.apereo.cas.util.cache.DistributedCacheObject;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DistributedCacheManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class DistributedCacheManagerTests {
    @Test
    public void verifyDefaults() {
        val mgr = new DistributedCacheManager<>() {
        };
        assertNull(mgr.get("key"));
        assertNotNull(mgr.getAll());
        assertNotNull(mgr.findAll(distributedCacheObject -> true));
        assertNotNull(mgr.getName());
        assertFalse(mgr.contains("key"));
        assertFalse(mgr.find(distributedCacheObject -> true).isPresent());

        assertDoesNotThrow(() -> mgr.set("key", new DistributedCacheObject("value")));
        assertDoesNotThrow(() -> mgr.set("key", new DistributedCacheObject("value")));
        assertDoesNotThrow(() -> mgr.remove("key", new DistributedCacheObject("value")));
    }
}
