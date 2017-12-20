package org.apereo.cas.monitor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.Assert.*;

/**
 * Unit test for {@link AbstractPoolHealthIndicator} class.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
@RunWith(JUnit4.class)
public class PoolHealthIndicatorTests {

    @Test
    public void verifyObserveOK() {
        final AbstractPoolHealthIndicator monitor = new AbstractPoolHealthIndicator(1000) {
            @Override
            protected Health.Builder checkPool(final Health.Builder builder) {
                return builder.up();
            }

            @Override
            protected int getIdleCount() {
                return 3;
            }

            @Override
            protected int getActiveCount() {
                return 2;
            }
        };
        final Health health = monitor.health();
        assertEquals(health.getStatus(), Status.UP);
    }

    @Test
    public void verifyObserveWarn() {
        final AbstractPoolHealthIndicator monitor = new AbstractPoolHealthIndicator(500) {
            @Override
            protected Health.Builder checkPool(final Health.Builder builder) throws Exception {
                Thread.sleep(1000);
                return builder.up();
            }

            @Override
            protected int getIdleCount() {
                return 1;
            }

            @Override
            protected int getActiveCount() {
                return 1;
            }
        };
        final Health health = monitor.health();
        assertEquals(health.getStatus().getCode(), "WARN");
    }

    @Test
    public void verifyObserveError() {
        final AbstractPoolHealthIndicator monitor = new AbstractPoolHealthIndicator(500) {
            @Override
            protected Health.Builder checkPool(final Health.Builder builder) throws Exception {
                throw new IllegalArgumentException("Pool check failed due to rogue penguins.");
            }

            @Override
            protected int getIdleCount() {
                return 1;
            }

            @Override
            protected int getActiveCount() {
                return 1;
            }
        };
        final Health health = monitor.health();
        assertEquals(health.getStatus(), Status.OUT_OF_SERVICE);
    }
}
