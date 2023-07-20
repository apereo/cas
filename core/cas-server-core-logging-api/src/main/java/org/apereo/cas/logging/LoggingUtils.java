package org.apereo.cas.logging;

import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.experimental.UtilityClass;
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
@UtilityClass
public class LoggingUtils {

    /**
     * Prepare log event log event.
     *
     * @param logEvent the log event
     * @return the log event
     */
    public LogEvent prepareLogEvent(final LogEvent logEvent) {
        val newLogEventBuilder = getLogEventBuilder(logEvent);
        return newLogEventBuilder.build();
    }

    /**
     * Gets log event builder.
     *
     * @param logEvent the log event
     * @return the log event builder
     */
    public Log4jLogEvent.Builder getLogEventBuilder(final LogEvent logEvent) {
        val messageModified = ApplicationContextProvider.getMessageSanitizer()
            .map(sanitizer -> sanitizer.sanitize(logEvent.getMessage().getFormattedMessage()))
            .orElseGet(() -> logEvent.getMessage().getFormattedMessage());
        val message = new SimpleMessage(messageModified);
        val contextData = new SortedArrayStringMap(logEvent.getContextData());
        val newLogEventBuilder = Log4jLogEvent.newBuilder()
            .setLevel(logEvent.getLevel())
            .setLoggerName(logEvent.getLoggerName())
            .setLoggerFqcn(logEvent.getLoggerFqcn())
            .setContextData(contextData)
            .setContextStack(logEvent.getContextStack())
            .setEndOfBatch(logEvent.isEndOfBatch())
            .setIncludeLocation(logEvent.isIncludeLocation())
            .setMarker(logEvent.getMarker())
            .setMessage(message)
            .setNanoTime(logEvent.getNanoTime())
            .setThreadName(logEvent.getThreadName())
            .setThrownProxy(logEvent.getThrownProxy())
            .setThrown(logEvent.getThrown())
            .setTimeMillis(logEvent.getTimeMillis());

        try {
            newLogEventBuilder.setSource(logEvent.getSource());
        } catch (final Exception e) {
            newLogEventBuilder.setSource(null);
        }
        return newLogEventBuilder;
    }
}
