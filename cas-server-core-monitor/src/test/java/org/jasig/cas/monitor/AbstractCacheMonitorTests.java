package org.jasig.cas.monitor;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit test for {@link AbstractCacheMonitor}.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
public class AbstractCacheMonitorTests {
    @Test
    public void verifyObserveOk() throws Exception {
        final AbstractCacheMonitor monitor = new AbstractCacheMonitor() {
            @Override
            protected SimpleCacheStatistics[] getStatistics() {
                return statsArray(new SimpleCacheStatistics(100, 200, 0));
            }
        };
        assertEquals(StatusCode.OK, monitor.observe().getCode());
    }

    @Test
    public void verifyObserveWarn() throws Exception {
        final AbstractCacheMonitor monitor = new AbstractCacheMonitor() {
            @Override
            protected SimpleCacheStatistics[] getStatistics() {
                return statsArray(new SimpleCacheStatistics(199, 200, 0));
            }
        };
        assertEquals(StatusCode.WARN, monitor.observe().getCode());
    }

    @Test
    public void verifyObserveError() throws Exception {
        final AbstractCacheMonitor monitor = new AbstractCacheMonitor() {
            @Override
            protected SimpleCacheStatistics[] getStatistics() {
                return statsArray(new SimpleCacheStatistics(100, 200, 1));
            }
        };
        assertEquals(StatusCode.WARN, monitor.observe().getCode());
    }


    @Test
    public void verifyObserveError2() throws Exception {
        // When cache has exceeded both thresholds, should report ERROR status
        final AbstractCacheMonitor monitor = new AbstractCacheMonitor() {
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
