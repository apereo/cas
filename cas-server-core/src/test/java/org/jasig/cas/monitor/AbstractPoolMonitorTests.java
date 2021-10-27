/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.monitor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link AbstractPoolMonitor} class.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
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
        monitor.setExecutor(this.executor);
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
        monitor.setExecutor(this.executor);
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
        monitor.setExecutor(this.executor);
        monitor.setMaxWait(500);
        final PoolStatus status = monitor.observe();
        assertEquals(StatusCode.ERROR, status.getCode());
        assertEquals(1, status.getIdleCount());
        assertEquals(1, status.getActiveCount());
    }
}
