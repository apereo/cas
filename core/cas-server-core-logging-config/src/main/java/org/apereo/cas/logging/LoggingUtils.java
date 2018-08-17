package org.apereo.cas.logging;

import org.apereo.cas.util.serialization.TicketIdSanitizationUtils;

import lombok.val;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.util.SortedArrayStringMap;

/**
 * This is {@link LoggingUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public final class LoggingUtils {

    private LoggingUtils() {
    }

    /**
     * Prepare log event log event.
     *
     * @param logEvent the log event
     * @return the log event
     */
    public static LogEvent prepareLogEvent(final LogEvent logEvent) {
        val messageModified = TicketIdSanitizationUtils.sanitize(logEvent.getMessage().getFormattedMessage());
        val message = new SimpleMessage(messageModified);
        val newLogEvent = Log4jLogEvent.newBuilder()
            .setLevel(logEvent.getLevel())
            .setLoggerName(logEvent.getLoggerName())
            .setLoggerFqcn(logEvent.getLoggerFqcn())
            .setContextData(new SortedArrayStringMap(logEvent.getContextData()))
            .setContextStack(logEvent.getContextStack())
            .setEndOfBatch(logEvent.isEndOfBatch())
            .setIncludeLocation(logEvent.isIncludeLocation())
            .setMarker(logEvent.getMarker())
            .setMessage(message)
            .setNanoTime(logEvent.getNanoTime())
            .setSource(logEvent.getSource())
            .setThreadName(logEvent.getThreadName())
            .setThrownProxy(logEvent.getThrownProxy())
            .setThrown(logEvent.getThrown())
            .setTimeMillis(logEvent.getTimeMillis())
            .build();
        return newLogEvent;
    }
}
