package org.jasig.cas.monitor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jasig.cas.ticket.registry.DefaultTicketRegistry;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for {@link HealthCheckMonitor} class.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public class HealthCheckMonitorTests {

    private HealthCheckMonitor monitor;

    @Before
    public void setUp() throws Exception {
        this.monitor = new HealthCheckMonitor();
    }

    @Test
    public void verifyObserveUnknown() throws Exception {
        assertEquals(StatusCode.UNKNOWN, this.monitor.observe().getCode());
    }

    @Test
    public void verifyObserveOk() throws Exception {
        final Set<Monitor> monitors = new HashSet<>();
        monitors.add(new MemoryMonitor());
        monitors.add(newSessionMonitor());
        this.monitor.setMonitors(monitors);
        assertEquals(StatusCode.OK, this.monitor.observe().getCode());
    }

    @Test
    public void verifyObserveWarn() throws Exception {
        final Set<Monitor> monitors = new HashSet<>();
        final MemoryMonitor memoryMonitor = new MemoryMonitor();
        memoryMonitor.setFreeMemoryWarnThreshold(100);
        monitors.add(memoryMonitor);
        monitors.add(newSessionMonitor());
        this.monitor.setMonitors(monitors);
        assertEquals(StatusCode.WARN, this.monitor.observe().getCode());
    }

    @Test
    public void verifyThrowsUncheckedException() throws Exception {
        final Monitor throwsUnchecked = new Monitor() {
            @Override
            public String getName() {
                return "ThrowsUnchecked";
            }

            @Override
            public Status observe() {
                throw new IllegalStateException("Boogity!");
            }
        };
        this.monitor.setMonitors(Collections.singleton(throwsUnchecked));
        assertEquals(StatusCode.ERROR, this.monitor.observe().getCode());
    }

    private SessionMonitor newSessionMonitor() {
        final SessionMonitor sessionMonitor = new SessionMonitor();
        sessionMonitor.setTicketRegistry(new DefaultTicketRegistry());
        return sessionMonitor;
    }
}
