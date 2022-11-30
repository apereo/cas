package org.apereo.cas.util;

import org.apereo.cas.util.function.FunctionUtils;

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

    private static final LogMessageSummarizer LOG_MESSAGE_SUMMARIZER;

    /*
     * Allow customization of whether this class will summarize stack traces when log level higher than debug.
     */
    static {
        LOG_MESSAGE_SUMMARIZER = ServiceLoader.load(LogMessageSummarizer.class)
                .findFirst()
                .orElse(new DefaultLogMessageSummarizer());
    }

    private static final int CHAR_REPEAT_ACCOUNT = 60;

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
     * @param context the context
     * @param message the message
     */
    public static void protocolMessage(final String title,
                                       final Map<String, Object> context,
                                       final Object message) {
        val builder = new StringBuilder();
        builder.append('\n');
        builder.append(StringUtils.repeat('=', CHAR_REPEAT_ACCOUNT));
        builder.append(String.format("\n%s\n", title));
        builder.append(StringUtils.repeat('=', CHAR_REPEAT_ACCOUNT));
        builder.append('\n');
        context.forEach((key, value) -> {
            val toLog = value.toString();
            if (StringUtils.isNotBlank(toLog)) {
                builder.append(String.format("%s: %s\n", key, toLog));
            }
        });
        builder.append(StringUtils.repeat('=', CHAR_REPEAT_ACCOUNT));
        if (message != null && StringUtils.isNotBlank(message.toString())) {
            builder.append(String.format("\n%s\n", message));
            builder.append(StringUtils.repeat('=', CHAR_REPEAT_ACCOUNT));
        }
        LoggerFactory.getLogger("PROTOCOL_MESSAGE").info(builder.toString());
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
                unused -> logger.error(msg, throwable),
                unused -> logger.error(LOG_MESSAGE_SUMMARIZER.summarizeStackTrace(msg, throwable)))
            .accept(throwable);
    }

    /**
     * Log Error.
     *
     * @param logger    the logger
     * @param throwable the throwable
     */
    public static void error(final Logger logger, final Throwable throwable) {
        error(logger, getMessage(throwable), throwable);
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
                unused -> logger.warn(message, throwable),
                unused -> logger.warn(LOG_MESSAGE_SUMMARIZER.summarizeStackTrace(message, throwable)))
            .accept(throwable);
    }

    /**
     * Get first non-null exception message, and return class name if all messages null.
     *
     * @param throwable Top level throwable
     * @return String containing first non-null exception message, or Throwable simple class name
     */
    static String getMessage(final Throwable throwable) {
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
