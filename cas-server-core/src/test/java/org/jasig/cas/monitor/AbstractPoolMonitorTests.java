/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */
package org.jasig.cas.monitor;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link AbstractPoolMonitor} class.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public class AbstractPoolMonitorTests {
    
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    @Test
    public void testObserveOK() throws Exception {
        final AbstractPoolMonitor monitor = new AbstractPoolMonitor() {
            protected StatusCode checkPool() throws Exception {
                return StatusCode.OK;
            }

            protected int getIdleCount() {
                return 3;
            }

            protected int getActiveCount() {
                return 2;
            }
        };
        monitor.setExecutor(executor);
        monitor.setMaxWait(1000);
        final PoolStatus status = monitor.observe();
        assertEquals(StatusCode.OK, status.getCode());
        assertEquals(3, status.getIdleCount());
        assertEquals(2, status.getActiveCount());
    }


    @Test
    public void testObserveWarn() throws Exception {
        final AbstractPoolMonitor monitor = new AbstractPoolMonitor() {
            protected StatusCode checkPool() throws Exception {
                Thread.sleep(1000);
                return StatusCode.OK;
            }

            protected int getIdleCount() {
                return 1;
            }

            protected int getActiveCount() {
                return 1;
            }
        };
        monitor.setExecutor(executor);
        monitor.setMaxWait(500);
        final PoolStatus status = monitor.observe();
        assertEquals(StatusCode.WARN, status.getCode());
        assertEquals(1, status.getIdleCount());
        assertEquals(1, status.getActiveCount());
    }


    @Test
    public void testObserveError() throws Exception {
        final AbstractPoolMonitor monitor = new AbstractPoolMonitor() {
            protected StatusCode checkPool() throws Exception {
                throw new RuntimeException("Pool check failed due to rogue penguins.");
            }

            protected int getIdleCount() {
                return 1;
            }

            protected int getActiveCount() {
                return 1;
            }
        };
        monitor.setExecutor(executor);
        monitor.setMaxWait(500);
        final PoolStatus status = monitor.observe();
        assertEquals(StatusCode.ERROR, status.getCode());
        assertEquals(1, status.getIdleCount());
        assertEquals(1, status.getActiveCount());
    }
}
