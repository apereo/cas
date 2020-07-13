package org.apereo.cas.util.cache;

import org.apereo.cas.util.PublisherIdentifier;

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
        val mgr = DistributedCacheManager.noOp();

        assertNull(mgr.get("key"));
        assertNotNull(mgr.getAll());
        assertNotNull(mgr.findAll(distributedCacheObject -> true));
        assertNotNull(mgr.getName());
        assertFalse(mgr.contains("key"));
        assertFalse(mgr.find(distributedCacheObject -> true).isPresent());

        val id = new PublisherIdentifier();
        val object = DistributedCacheObject.<String>builder()
            .value("value")
            .publisherIdentifier(id)
            .build();
        
        assertDoesNotThrow(() -> mgr.set("key", object, true));
        assertDoesNotThrow(() -> mgr.set("key", object, true));
        assertDoesNotThrow(() -> mgr.remove("key", object, true));
        assertNotNull(mgr.update("key", object, true));
        mgr.close();
    }
}
