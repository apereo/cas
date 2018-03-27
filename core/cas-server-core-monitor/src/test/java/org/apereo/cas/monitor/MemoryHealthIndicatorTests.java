package org.apereo.cas.monitor;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.boot.actuate.health.Status;

import static org.junit.Assert.*;

/**
 * Unit test for {@link MemoryMonitor} class.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
@RunWith(JUnit4.class)
@Slf4j
public class MemoryHealthIndicatorTests {

    @Test
    public void verifyObserveOk() {
        assertEquals(Status.UP, new MemoryMonitor(0).health().getStatus());
    }

    @Test
    public void verifyObserveWarn() {
        final MemoryMonitor monitor = new MemoryMonitor(100);
        assertEquals(Status.DOWN, monitor.health().getStatus());
    }
}
