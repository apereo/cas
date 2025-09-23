package org.apereo.cas.util;

import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.logging.DefaultLogMessageSummarizer;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * This is {@link LoggingUtils}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@UtilityClass
public class LoggingUtils {

    private static final int CHAR_REPEAT_ACCOUNT = 60;

    private static final String LOGGER_NAME_PROTOCOL_MESSAGE = "PROTOCOL_MESSAGE";

    private static final LogMessageSummarizer LOG_MESSAGE_SUMMARIZER;

    /*
     * Allow customization of whether this class will summarize stack traces when log level higher than debug.
     */
    static {
        LOG_MESSAGE_SUMMARIZER = ServiceLoader.load(LogMessageSummarizer.class)
            .findFirst()
            .orElseGet(DefaultLogMessageSummarizer::new);
    }

    /**
     * Protocol message.
     *
     * @param title   the title
     * @param context the context
     */
    public static void protocolMessage(final String title,
                                       final Map<String, Object> context) {
        protocolMessage(title, context, StringUtils.EMPTY);
    }

    /**
     * Protocol message.
     *
     * @param title   the title
     * @param message the message
     */
    public static void protocolMessage(final String title, final Object message) {
        protocolMessage(title, Map.of(), message);
    }

    /**
     * Protocol message.
     *
     * @param title   the title
     * @param context the context
     * @param message the message
     */
    public static void protocolMessage(final String title,
                                       final Map<String, Object> context,
                                       final Object message) {
        if (isProtocolMessageLoggerEnabled()) {
            val builder = new StringBuilder();
            builder.append('\n');
            builder.append(StringUtils.repeat('=', CHAR_REPEAT_ACCOUNT));
            builder.append(String.format("\n%s\n", title));
            builder.append(StringUtils.repeat('=', CHAR_REPEAT_ACCOUNT));
            builder.append('\n');
            context.forEach((key, value) -> {
                val toLog = DigestUtils.abbreviate(value.toString());
                if (StringUtils.isNotBlank(toLog)) {
                    builder.append(String.format("%s: %s\n", key, toLog));
                }
            });
            if (!context.isEmpty()) {
                builder.append(StringUtils.repeat('=', CHAR_REPEAT_ACCOUNT));
                builder.append('\n');
            }
            if (message != null && StringUtils.isNotBlank(message.toString())) {
                builder.append(String.format("%s\n", message));
                builder.append(StringUtils.repeat('=', CHAR_REPEAT_ACCOUNT));
            }
            val result = builder.toString();
            LoggerFactory.getLogger(LOGGER_NAME_PROTOCOL_MESSAGE).info(result);
        }
    }

    public static boolean isProtocolMessageLoggerEnabled() {
        return LoggerFactory.getLogger(LOGGER_NAME_PROTOCOL_MESSAGE).isInfoEnabled();
    }

    /**
     * Error.
     *
     * @param logger the logger
     * @param msg    the msg
     */
    public static void error(final Logger logger, final String msg) {
        logger.error(msg);
    }

    /**
     * Error.
     *
     * @param logger    the logger
     * @param msg       the msg
     * @param throwable the throwable
     */
    public static void error(final Logger logger, final String msg, final Throwable throwable) {
        FunctionUtils.doIf(LOG_MESSAGE_SUMMARIZER.shouldSummarize(logger),
                __ -> logger.error(LOG_MESSAGE_SUMMARIZER.summarizeStackTrace(msg, throwable)),
                __ -> logger.error(msg, throwable))
            .accept(throwable);
    }

    /**
     * Log Error.
     *
     * @param logger    the logger
     * @param throwable the throwable
     */
    public static void error(final Logger logger, final Throwable throwable) {
        if (throwable != null) {
            error(logger, getMessage(throwable), throwable);
        }
    }

    /**
     * Log warning.
     *
     * @param logger    the logger
     * @param throwable the throwable
     */
    public static void warn(final Logger logger, final Throwable throwable) {
        warn(logger, getMessage(throwable), throwable);
    }

    /**
     * Log warning.
     *
     * @param logger    the logger
     * @param message   the message
     * @param throwable the throwable
     */
    public static void warn(final Logger logger, final String message, final Throwable throwable) {
        FunctionUtils.doIf(LOG_MESSAGE_SUMMARIZER.shouldSummarize(logger),
                __ -> logger.warn(LOG_MESSAGE_SUMMARIZER.summarizeStackTrace(message, throwable)),
                __ -> logger.warn(message, throwable))
            .accept(throwable);
    }

    /**
     * Get first non-null exception message, and return class name if all messages null.
     *
     * @param throwable Top level throwable
     * @return String containing first non-null exception message, or Throwable simple class name
     */
    public static String getMessage(final Throwable throwable) {
        if (StringUtils.isEmpty(throwable.getMessage())) {
            val message = ExceptionUtils.getThrowableList(throwable)
                .stream().map(Throwable::getMessage).filter(Objects::nonNull).findFirst();
            if (message.isPresent()) {
                return message.get();
            }
        }
        return StringUtils.defaultIfEmpty(throwable.getMessage(), throwable.getClass().getSimpleName());
    }

}
