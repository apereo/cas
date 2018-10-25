package org.apereo.cas.monitor;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link MemoryMonitor} class.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public class MemoryHealthIndicatorTests {

    @Test
    public void verifyObserveOk() {
        assertEquals(Status.UP, new MemoryMonitor(0).health().getStatus());
    }

    @Test
    public void verifyObserveWarn() {
        val monitor = new MemoryMonitor(100);
        assertEquals(Status.DOWN, monitor.health().getStatus());
    }
}
