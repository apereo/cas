package org.apereo.cas.cache;

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
        assertNotNull(o.toString());
        assertNotNull(o.getValue());
        assertTrue(o.getTimestamp() > 0);
        assertTrue(o.getProperties().isEmpty());
        o.getProperties().put("key", "value");
        assertFalse(o.getProperties().isEmpty());
        assertNotNull(o.getProperty("key", String.class));
        assertTrue(o.containsProperty("key"));
    }

    @Test
    public void verifyNullValue() {
        val o = new DistributedCacheObject<String>("objectValue");
        assertTrue(o.getProperties().isEmpty());
        o.getProperties().put("key", null);
        o.getProperties().put("key2", 12.54);
        assertNull(o.getProperty("nothing", String.class));
        assertNull(o.getProperty("key", String.class));
        assertThrows(ClassCastException.class, () -> o.getProperty("key2", Long.class));
    }
}
