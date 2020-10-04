package org.apereo.cas.logging;

import lombok.SneakyThrows;
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
public class ExceptionOnlyFilterTests {

    /**
     * Test that only log messages with Exception pass the {@link ExceptionOnlyFilter} filter.
     * This test implicitly should test generation of
     * META-INF/org/apache/logging/log4j/core/config/plugins/Log4j2Plugins.dat
     * as long as log4j2-test.xml doesn't have packages attribute in root element.
     * Stopping th log system so file is flushed for size check, sleep also works.
     */
    @Test
    public void verifyOperation() {
        val fileSize = getFileSize();
        LOGGER.error("Testing no exception");
        sleep(1000);
        assertEquals(fileSize, getFileSize());
        LOGGER.error("Testing with exception", new Exception());
        sleep(1000);
        assertTrue(getFileSize() > fileSize);
    }

    @Test
    public void verifyFilters() {
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

    private static long getFileSize() {
        var logFile = FileUtils.getFile("build/slf4j-exceptions.log");
        assertTrue(logFile.exists(), "Log file not found");
        return logFile.length();
    }

    @SneakyThrows
    private static void sleep(final int millis) {
        Thread.sleep(millis);
    }


}
