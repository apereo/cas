package org.apereo.cas.monitor;

import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link AbstractCacheHealthIndicator}.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    AopAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Metrics")
public class CacheHealthIndicatorTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    protected static SimpleCacheStatistics[] statsArray(final SimpleCacheStatistics... statistics) {
        return statistics;
    }

    @Test
    public void verifyObserveOk() {
        val warn = casProperties.getMonitor().getWarn();
        val monitor = new AbstractCacheHealthIndicator(
            warn.getEvictionThreshold(),
            warn.getThreshold()) {
            @Override
            protected SimpleCacheStatistics[] getStatistics() {
                return statsArray(new SimpleCacheStatistics(100, 200, 0));
            }
        };
        val status = monitor.health().getStatus();
        assertEquals(Status.UP, status);
    }

    @Test
    public void verifyObserveWarn() {
        val warn = casProperties.getMonitor().getWarn();
        val monitor = new AbstractCacheHealthIndicator(
            warn.getEvictionThreshold(),
            warn.getThreshold()
        ) {
            @Override
            protected SimpleCacheStatistics[] getStatistics() {
                return statsArray(new SimpleCacheStatistics(199, 200, 100));
            }
        };
        val status = monitor.health().getStatus();
        assertEquals("WARN", status.getCode());
    }

    @Test
    public void verifyObserveError() {
        val warn = casProperties.getMonitor().getWarn();
        val monitor = new AbstractCacheHealthIndicator(
            warn.getEvictionThreshold(),
            warn.getThreshold()) {
            @Override
            protected SimpleCacheStatistics[] getStatistics() {
                return statsArray(new SimpleCacheStatistics(100, 110, 0));
            }
        };
        val status = monitor.health().getStatus();
        assertEquals(Status.OUT_OF_SERVICE, status);
    }

    @Test
    public void verifyObserveError2() {
        val warn = casProperties.getMonitor().getWarn();
        val monitor = new AbstractCacheHealthIndicator(
            warn.getEvictionThreshold(),
            warn.getThreshold()) {
            @Override
            protected SimpleCacheStatistics[] getStatistics() {
                return statsArray(new SimpleCacheStatistics(199, 200, 1));
            }
        };
        assertEquals("WARN", monitor.health().getStatus().getCode());
    }

    @Test
    public void verifyToString() {
        val stat = new SimpleCacheStatistics(100, 110, 0, "test");
        assertNotNull(stat.toString(new StringBuilder()));
    }

    @Test
    public void verifyOut() {
        val indicator = new AbstractCacheHealthIndicator(0, 0) {
            @Override
            protected CacheStatistics[] getStatistics() {
                return null;
            }
        };
        assertEquals(Status.OUT_OF_SERVICE, indicator.health().getStatus());
    }

    @Test
    public void verifyDown() {
        val indicator = new AbstractCacheHealthIndicator(0, 0) {
            @Override
            protected CacheStatistics[] getStatistics() {
                return new CacheStatistics[]{
                    mock(CacheStatistics.class)
                };
            }

            @Override
            protected Status status(final CacheStatistics statistics) {
                return Status.DOWN;
            }
        };
        assertEquals(Status.DOWN, indicator.health().getStatus());
    }

    @Test
    public void verifyError() {
        val indicator = new AbstractCacheHealthIndicator(0, 0) {
            @Override
            protected CacheStatistics[] getStatistics() {
                throw new IllegalArgumentException();
            }
        };
        assertEquals(Status.DOWN, indicator.health().getStatus());
    }
}
