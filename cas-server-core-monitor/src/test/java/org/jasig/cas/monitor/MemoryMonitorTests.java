package org.jasig.cas.monitor;

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
    public void verifyObserveOk() throws Exception {
        assertEquals(StatusCode.OK, new MemoryMonitor().observe().getCode());
    }

    @Test
    public void verifyObserveWarn() throws Exception {
        final MemoryMonitor monitor = new MemoryMonitor();
        monitor.setFreeMemoryWarnThreshold(100);
        assertEquals(StatusCode.WARN, monitor.observe().getCode());
    }
}
