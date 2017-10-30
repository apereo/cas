package org.apereo.cas.monitor;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for {@link MemoryMonitor} class.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public class MemoryMonitorTests {

    @Test
    public void verifyObserveOk() {
        assertEquals(StatusCode.OK, new MemoryMonitor(0).observe().getCode());
    }

    @Test
    public void verifyObserveWarn() {
        final MemoryMonitor monitor = new MemoryMonitor(100);
        assertEquals(StatusCode.WARN, monitor.observe().getCode());
    }
}
