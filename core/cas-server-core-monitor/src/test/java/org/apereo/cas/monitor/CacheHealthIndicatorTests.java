package org.apereo.cas.monitor;

import org.junit.Test;
import org.springframework.boot.actuate.health.Status;

import static org.junit.Assert.*;

/**
 * Unit test for {@link AbstractCacheHealthIndicator}.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
public class CacheHealthIndicatorTests {

    @Test
    public void verifyObserveOk() {
        final AbstractCacheHealthIndicator monitor = new AbstractCacheHealthIndicator() {
            @Override
            protected SimpleCacheStatistics[] getStatistics() {
                return statsArray(new SimpleCacheStatistics(100, 200, 0));
            }
        };
        assertEquals(Status.UP, monitor.health().getStatus());
    }

    @Test
    public void verifyObserveWarn() {
        final AbstractCacheHealthIndicator monitor = new AbstractCacheHealthIndicator() {
            @Override
            protected SimpleCacheStatistics[] getStatistics() {
                return statsArray(new SimpleCacheStatistics(199, 200, 0));
            }
        };
        assertEquals("WARN", monitor.health().getStatus().getCode());
    }

    @Test
    public void verifyObserveError() {
        final AbstractCacheHealthIndicator monitor = new AbstractCacheHealthIndicator() {
            @Override
            protected SimpleCacheStatistics[] getStatistics() {
                return statsArray(new SimpleCacheStatistics(100, 200, 1));
            }
        };
        assertEquals("WARN", monitor.health().getStatus().getCode());
    }


    @Test
    public void verifyObserveError2() {
        // When cache has exceeded both thresholds, should report ERROR status
        final AbstractCacheHealthIndicator monitor = new AbstractCacheHealthIndicator() {
            @Override
            protected SimpleCacheStatistics[] getStatistics() {
                return statsArray(new SimpleCacheStatistics(199, 200, 1));
            }
        };
        assertEquals("WARN", monitor.health().getStatus().getCode());
    }

    protected static SimpleCacheStatistics[] statsArray(final SimpleCacheStatistics... statistics) {
        return statistics;
    }
}
