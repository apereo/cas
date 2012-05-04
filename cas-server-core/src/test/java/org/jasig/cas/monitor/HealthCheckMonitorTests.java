/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
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
