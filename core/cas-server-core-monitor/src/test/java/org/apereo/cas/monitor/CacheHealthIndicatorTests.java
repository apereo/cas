package org.apereo.cas.monitor;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.config.support.EnvironmentConversionServiceInitializer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * Unit test for {@link AbstractCacheHealthIndicator}.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RefreshAutoConfiguration.class})
@ContextConfiguration(initializers = EnvironmentConversionServiceInitializer.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CacheHealthIndicatorTests {

    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Test
    public void verifyObserveOk() {
        final AbstractCacheHealthIndicator monitor = new AbstractCacheHealthIndicator(casProperties) {
            @Override
            protected SimpleCacheStatistics[] getStatistics() {
                return statsArray(new SimpleCacheStatistics(100, 200, 0));
            }
        };
        final Status status = monitor.health().getStatus();
        assertEquals(Status.UP, status);
    }

    @Test
    public void verifyObserveWarn() {
        final AbstractCacheHealthIndicator monitor = new AbstractCacheHealthIndicator(casProperties) {
            @Override
            protected SimpleCacheStatistics[] getStatistics() {
                return statsArray(new SimpleCacheStatistics(199, 200, 100));
            }
        };
        final Status status = monitor.health().getStatus();
        assertEquals("WARN", status.getCode());
    }

    @Test
    public void verifyObserveError() {
        final AbstractCacheHealthIndicator monitor = new AbstractCacheHealthIndicator(casProperties) {
            @Override
            protected SimpleCacheStatistics[] getStatistics() {
                return statsArray(new SimpleCacheStatistics(100, 110, 0));
            }
        };
        final Status status = monitor.health().getStatus();
        assertEquals(Status.OUT_OF_SERVICE, status);
    }
    
    @Test
    public void verifyObserveError2() {
        // When cache has exceeded both thresholds, should report ERROR status
        final AbstractCacheHealthIndicator monitor = new AbstractCacheHealthIndicator(casProperties) {
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
