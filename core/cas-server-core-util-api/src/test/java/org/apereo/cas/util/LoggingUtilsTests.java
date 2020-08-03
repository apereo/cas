package org.apereo.cas.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link LoggingUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Utility")
@Slf4j
public class LoggingUtilsTests {
    @Test
    public void verifyOperation() {
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                LoggingUtils.error(LOGGER, "error", new RuntimeException("error"));
                LoggingUtils.error(LOGGER, new RuntimeException("error"));
                LoggingUtils.warn(LOGGER, "error", new RuntimeException("error"));
                LoggingUtils.warn(LOGGER, new RuntimeException("error"));
            }
        });
    }

}
