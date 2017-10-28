package org.apereo.cas.monitor;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit test for {@link AbstractCacheMonitor}.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
public class AbstractCacheMonitorTests {

    private static final String MONITOR_NAME = "monitor";

    @Test
    public void verifyObserveOk() {
        final AbstractCacheMonitor monitor = new AbstractCacheMonitor(MONITOR_NAME) {
            @Override
            protected SimpleCacheStatistics[] getStatistics() {
                return statsArray(new SimpleCacheStatistics(100, 200, 0));
            }
        };
        assertEquals(StatusCode.OK, monitor.observe().getCode());
    }

    @Test
    public void verifyObserveWarn() {
        final AbstractCacheMonitor monitor = new AbstractCacheMonitor(MONITOR_NAME) {
            @Override
            protected SimpleCacheStatistics[] getStatistics() {
                return statsArray(new SimpleCacheStatistics(199, 200, 0));
            }
        };
        assertEquals(StatusCode.WARN, monitor.observe().getCode());
    }

    @Test
    public void verifyObserveError() {
        final AbstractCacheMonitor monitor = new AbstractCacheMonitor(MONITOR_NAME) {
            @Override
            protected SimpleCacheStatistics[] getStatistics() {
                return statsArray(new SimpleCacheStatistics(100, 200, 1));
            }
        };
        assertEquals(StatusCode.WARN, monitor.observe().getCode());
    }


    @Test
    public void verifyObserveError2() {
        // When cache has exceeded both thresholds, should report ERROR status
        final AbstractCacheMonitor monitor = new AbstractCacheMonitor(MONITOR_NAME) {
            @Override
            protected SimpleCacheStatistics[] getStatistics() {
                return statsArray(new SimpleCacheStatistics(199, 200, 1));
            }
        };
        assertEquals(StatusCode.WARN, monitor.observe().getCode());
    }

    protected static SimpleCacheStatistics[] statsArray(final SimpleCacheStatistics... statistics) {
        return statistics;
    }
}
