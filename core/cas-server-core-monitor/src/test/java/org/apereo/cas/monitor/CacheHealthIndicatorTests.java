package org.apereo.cas.monitor;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.health.contributor.Status;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link AbstractCacheHealthIndicator}.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = AopAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Metrics")
@ExtendWith(CasTestExtension.class)
class CacheHealthIndicatorTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    protected static SimpleCacheStatistics[] statsArray(final SimpleCacheStatistics... statistics) {
        return statistics;
    }

    @Test
    void verifyObserveOk() {
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
    void verifyObserveWarn() {
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
    void verifyObserveError() {
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
    void verifyObserveError2() {
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
    void verifyToString() {
        val stat = new SimpleCacheStatistics(100, 110, 0, "test");
        assertNotNull(stat.toString(new StringBuilder()));
    }

    @Test
    void verifyOut() {
        val indicator = new AbstractCacheHealthIndicator(0, 0) {
            @Override
            protected CacheStatistics[] getStatistics() {
                return null;
            }
        };
        assertEquals(Status.OUT_OF_SERVICE, indicator.health().getStatus());
    }

    @Test
    void verifyDown() {
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
    void verifyError() {
        val indicator = new AbstractCacheHealthIndicator(0, 0) {
            @Override
            protected CacheStatistics[] getStatistics() {
                throw new IllegalArgumentException();
            }
        };
        assertEquals(Status.DOWN, indicator.health().getStatus());
    }
}
