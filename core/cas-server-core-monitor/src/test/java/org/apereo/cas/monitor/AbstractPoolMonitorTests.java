package org.apereo.cas.monitor;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

/**
 * Unit test for {@link AbstractPoolMonitor} class.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public class AbstractPoolMonitorTests {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Test
    public void verifyObserveOK() {
        final AbstractPoolMonitor monitor = new AbstractPoolMonitor("monitor", executor, 1000) {
            @Override
            protected StatusCode checkPool() {
                return StatusCode.OK;
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
        final PoolStatus status = monitor.observe();
        assertEquals(StatusCode.OK, status.getCode());
        assertEquals(3, status.getIdleCount());
        assertEquals(2, status.getActiveCount());
    }

    @Test
    public void verifyObserveWarn() {
        final AbstractPoolMonitor monitor = new AbstractPoolMonitor("monitor", executor, 500) {
            @Override
            protected StatusCode checkPool() throws Exception {
                Thread.sleep(1000);
                return StatusCode.OK;
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
        final PoolStatus status = monitor.observe();
        assertEquals(StatusCode.WARN, status.getCode());
        assertEquals(1, status.getIdleCount());
        assertEquals(1, status.getActiveCount());
    }

    @Test
    public void verifyObserveError() {
        final AbstractPoolMonitor monitor = new AbstractPoolMonitor("monitor", executor, 500) {
            @Override
            protected StatusCode checkPool() {
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
        final PoolStatus status = monitor.observe();
        assertEquals(StatusCode.ERROR, status.getCode());
        assertEquals(1, status.getIdleCount());
        assertEquals(1, status.getActiveCount());
    }
}
