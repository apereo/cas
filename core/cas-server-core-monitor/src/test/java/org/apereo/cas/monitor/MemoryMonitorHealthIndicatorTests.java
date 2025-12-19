package org.apereo.cas.monitor;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Status;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link MemoryMonitorHealthIndicator} class.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
@Tag("Metrics")
class MemoryMonitorHealthIndicatorTests {

    @Test
    void verifyObserveOk() {
        assertEquals(Status.UP, new MemoryMonitorHealthIndicator(0).health().getStatus());
    }

    @Test
    void verifyObserveWarn() {
        val monitor = new MemoryMonitorHealthIndicator(100);
        assertEquals(Status.DOWN, monitor.health().getStatus());
    }
}
