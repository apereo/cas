package org.apereo.cas.monitor;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link MemoryMonitorHealthIndicator} class.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
@Tag("Simple")
public class MemoryHealthIndicatorTests {

    @Test
    public void verifyObserveOk() {
        assertEquals(Status.UP, new MemoryMonitorHealthIndicator(0).health().getStatus());
    }

    @Test
    public void verifyObserveWarn() {
        val monitor = new MemoryMonitorHealthIndicator(100);
        assertEquals(Status.DOWN, monitor.health().getStatus());
    }
}
