package org.apereo.cas.monitor;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AbstractCacheHealthIndicatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class AbstractCacheHealthIndicatorTests {

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
