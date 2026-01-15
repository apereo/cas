package org.apereo.cas.git;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link LoggingGitProgressMonitorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Git")
class LoggingGitProgressMonitorTests {
    @Test
    void verifyOperation() {
        val monitor = new LoggingGitProgressMonitor();
        assertDoesNotThrow(() -> {
            monitor.onUpdate("taskName", 10, Duration.ZERO);
            monitor.onUpdate("taskName", 10, 100, 10, Duration.ZERO);
            monitor.onEndTask("taskName", 10, 100, 10, Duration.ZERO);
            monitor.onEndTask("taskName", 10, 100, 10, Duration.ZERO);
        });
    }
}
