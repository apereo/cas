package org.apereo.cas.util;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.UndeclaredThrowableException;

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
        assertDoesNotThrow(() -> {
            LoggingUtils.error(LOGGER, "error", new RuntimeException("error"));
            LoggingUtils.error(LOGGER, new RuntimeException("error"));
            LoggingUtils.warn(LOGGER, "error", new RuntimeException("error"));
            LoggingUtils.warn(LOGGER, new RuntimeException("error"));
        });
    }

    @Test
    public void verifyGetNonNullMessage() {
        val exception = new UndeclaredThrowableException(new Exception("nested"));
        assertEquals("nested", LoggingUtils.getMessage(exception));
        val exception2 = new Exception("first", new Exception("second"));
        assertEquals("first", LoggingUtils.getMessage(exception2));
        val exception3 = new UndeclaredThrowableException(new Exception(new RuntimeException()));
        assertEquals("java.lang.RuntimeException", LoggingUtils.getMessage(exception3));
    }
}
