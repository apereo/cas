package org.apereo.cas.logging;

import module java.base;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ExceptionOnlyFilterTests}.
 *
 * @author Hal Deadman
 * @since 6.3.0
 */
@Tag("Simple")
@Slf4j
class ExceptionOnlyFilterTests {

    private static File getLogFile() {
        val logFile = FileUtils.getFile("build/slf4j-exceptions.log");
        assertTrue(logFile.exists(), "Log file not found");
        return logFile;
    }

    private static long getFileSize() {
        return getLogFile().length();
    }

    /**
     * Test that only log messages with Exception pass the {@link ExceptionOnlyFilter} filter.
     * This test implicitly should test generation of
     * META-INF/org/apache/logging/log4j/core/config/plugins/Log4j2Plugins.dat
     * as long as log4j2-test.xml doesn't have packages attribute in root element.
     */
    @Test
    void verifyOperation() throws Exception {
        val fileSize = getFileSize();
        LOGGER.error("Testing no exception");
        LOGGER.error("Testing with exception", new Exception());
        await().atMost(Duration.of(5, ChronoUnit.SECONDS)).until(() -> getFileSize() > fileSize);
        val contents = FileUtils.readFileToString(getLogFile(), StandardCharsets.UTF_8);
        assertFalse(contents.contains("Testing no exception"));
        assertTrue(contents.contains("Testing with exception"));
    }

    @Test
    void verifyFilters() {
        val filter = new ExceptionOnlyFilter();
        assertEquals(Filter.Result.ACCEPT,
            filter.filter(mock(Logger.class), Level.INFO, mock(Marker.class), mock(Message.class), new Throwable()));
        assertEquals(Filter.Result.DENY,
            filter.filter(mock(Logger.class), Level.INFO, mock(Marker.class), mock(Message.class), null));

        assertEquals(Filter.Result.ACCEPT,
            filter.filter(mock(Logger.class), Level.INFO, mock(Marker.class), new Object(), new Throwable()));
        assertEquals(Filter.Result.DENY,
            filter.filter(mock(Logger.class), Level.INFO, mock(Marker.class), new Object(), null));

        assertEquals(Filter.Result.ACCEPT,
            filter.filter(mock(Logger.class), Level.INFO, mock(Marker.class), "message", "value1", new Throwable()));
        assertEquals(Filter.Result.DENY,
            filter.filter(mock(Logger.class), Level.INFO, mock(Marker.class), "message", "value1", "value2"));
    }


}
