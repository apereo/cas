package org.apereo.cas;

import org.apereo.cas.util.cache.DistributedCacheObject;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DistributedCacheObjectTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Simple")
public class DistributedCacheObjectTests {
    @Test
    public void verifyAction() {
        val o = new DistributedCacheObject<String>("objectValue");
        assertTrue(o.getProperties().isEmpty());
        o.getProperties().put("key", "value");
        assertFalse(o.getProperties().isEmpty());
        assertNotNull(o.getProperty("key", String.class));
        assertTrue(o.containsProperty("key"));
    }
}
