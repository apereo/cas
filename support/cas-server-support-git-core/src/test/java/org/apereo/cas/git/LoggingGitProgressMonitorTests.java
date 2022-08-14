package org.apereo.cas.git;

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
public class LoggingGitProgressMonitorTests {
    @Test
    public void verifyOperation() {
        val monitor = new LoggingGitProgressMonitor();
        assertDoesNotThrow(() -> {
            monitor.onUpdate("taskName", 10);
            monitor.onUpdate("taskName", 10, 100, 10);
            monitor.onEndTask("taskName", 10, 100, 10);
            monitor.onEndTask("taskName", 10, 100, 10);
        });
    }
}
