package org.apereo.cas.util;

import module java.base;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link LoggingUtilsSummarizeTests} configured with log level higher than debug to trigger summarize logic.
 *
 * @author Hal Deadman
 * @since 6.6.0
 */
@Tag("Utility")
@Slf4j
class LoggingUtilsSummarizeTests {
    @Test
    void verifyOperation() {
        assertDoesNotThrow(() -> {
            LoggingUtils.error(LOGGER, "error", new RuntimeException("error"));
            LoggingUtils.warn(LOGGER, "error", new RuntimeException("error"));
        });
    }
}
