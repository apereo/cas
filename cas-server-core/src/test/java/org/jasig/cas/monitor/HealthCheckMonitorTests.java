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

import org.jasig.cas.ticket.registry.DefaultTicketRegistry;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link HealthCheckMonitor} class.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public class HealthCheckMonitorTests {

    private HealthCheckMonitor monitor;

    @Before
    public void setUp() throws Exception {
        monitor = new HealthCheckMonitor();
    }

    @Test
    public void testObserveUnknown() throws Exception {
        assertEquals(StatusCode.UNKNOWN, monitor.observe().getCode());
    }

    @Test
    public void testObserveOk() throws Exception {
        final Set<Monitor> monitors = new HashSet<Monitor>();
        monitors.add(new MemoryMonitor());
        monitors.add(newSessionMonitor());
        monitor.setMonitors(monitors);
        assertEquals(StatusCode.OK, monitor.observe().getCode());
    }

    @Test
    public void testObserveWarn() throws Exception {
        final Set<Monitor> monitors = new HashSet<Monitor>();
        final MemoryMonitor memoryMonitor = new MemoryMonitor();
        memoryMonitor.setFreeMemoryWarnThreshold(100);
        monitors.add(memoryMonitor);
        monitors.add(newSessionMonitor());
        monitor.setMonitors(monitors);
        assertEquals(StatusCode.WARN, monitor.observe().getCode());
    }
    
    private SessionMonitor newSessionMonitor() {
        final SessionMonitor sessionMonitor = new SessionMonitor();
        sessionMonitor.setTicketRegistry(new DefaultTicketRegistry());
        return sessionMonitor;
    }
}
