package org.jasig.cas.monitor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

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
    public void verifyObserveOK() throws Exception {
        final AbstractPoolMonitor monitor = new AbstractPoolMonitor() {
            @Override
            protected StatusCode checkPool() throws Exception {
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
        monitor.setExecutor(this.executor);
        monitor.setMaxWait(1000);
        final PoolStatus status = monitor.observe();
        assertEquals(StatusCode.OK, status.getCode());
        assertEquals(3, status.getIdleCount());
        assertEquals(2, status.getActiveCount());
    }


    @Test
    public void verifyObserveWarn() throws Exception {
        final AbstractPoolMonitor monitor = new AbstractPoolMonitor() {
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
        monitor.setExecutor(this.executor);
        monitor.setMaxWait(500);
        final PoolStatus status = monitor.observe();
        assertEquals(StatusCode.WARN, status.getCode());
        assertEquals(1, status.getIdleCount());
        assertEquals(1, status.getActiveCount());
    }


    @Test
    public void verifyObserveError() throws Exception {
        final AbstractPoolMonitor monitor = new AbstractPoolMonitor() {
            @Override
            protected StatusCode checkPool() throws Exception {
                throw new RuntimeException("Pool check failed due to rogue penguins.");
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
        monitor.setExecutor(this.executor);
        monitor.setMaxWait(500);
        final PoolStatus status = monitor.observe();
        assertEquals(StatusCode.ERROR, status.getCode());
        assertEquals(1, status.getIdleCount());
        assertEquals(1, status.getActiveCount());
    }
}
