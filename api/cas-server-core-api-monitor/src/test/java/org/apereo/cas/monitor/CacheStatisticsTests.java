package org.apereo.cas.monitor;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CacheStatisticsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Simple")
class CacheStatisticsTests {
    @Test
    void verifyOperation() throws Throwable {
        val stats = new CacheStatistics() {
            @Override
            public String getName() {
                return "Cache";
            }

            @Override
            public String toString(final StringBuilder builder) {
                return "Cache";
            }
        };
        assertEquals(0, stats.getCapacity());
        assertEquals(0, stats.getEvictions());
        assertEquals(0, stats.getPercentFree());
        assertEquals(0, stats.getSize());
    }

}
