package org.apereo.cas.persondir.cache;

import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link HashCodeCacheKeyTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("AttributeRepository")
class HashCodeCacheKeyTests {
    @Test
    void verifyOperation() {
        val key = new HashCodeCacheKey(System.nanoTime(), RandomUtils.nextInt());
        assertEquals(key, key);
        assertNotEquals(new Object(), key);
        val key2 = new HashCodeCacheKey(System.nanoTime(), RandomUtils.nextInt());
        assertNotEquals(key, key2);
        val key3 = new HashCodeCacheKey(key.getCheckSum(), RandomUtils.nextInt());
        assertNotEquals(key, key3);
        assertTrue(key3.getHashCode() > 0);
    }
}
