package org.apereo.cas.monitor;

import org.apereo.cas.ticket.registry.DefaultTicketRegistry;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

/**
 * Unit test for {@link HealthCheckMonitor} class.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public class HealthCheckMonitorTests {

    @Test
    public void verifyObserveUnknown() {
        final HealthCheckMonitor monitor = new HealthCheckMonitor(new HashSet<>(0));

        assertEquals(StatusCode.UNKNOWN, monitor.observe().getCode());
    }

    @Test
    public void verifyObserveOk() {
        final Set<Monitor> monitors = new HashSet<>(asList(new MemoryMonitor(0), newSessionMonitor()));
        final HealthCheckMonitor monitor = new HealthCheckMonitor(monitors);
        assertEquals(StatusCode.OK, monitor.observe().getCode());
    }

    @Test
    public void verifyObserveWarn() {
        final Set<Monitor> monitors = new HashSet<>(asList(new MemoryMonitor(100), newSessionMonitor()));
        final HealthCheckMonitor monitor = new HealthCheckMonitor(monitors);
        assertEquals(StatusCode.WARN, monitor.observe().getCode());
    }

    @Test
    public void verifyThrowsUncheckedException() {
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
        final HealthCheckMonitor monitor = new HealthCheckMonitor(Collections.singleton(throwsUnchecked));
        assertEquals(StatusCode.ERROR, monitor.observe().getCode());
    }

    private static SessionMonitor newSessionMonitor() {
        return new SessionMonitor(new DefaultTicketRegistry(), 0, 0);
    }
}
