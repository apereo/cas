package org.apereo.cas.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;

/**
 * This is {@link LoggingUtils}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@UtilityClass
public class LoggingUtils {

    /**
     * Error.
     *
     * @param logger    the logger
     * @param msg       the msg
     * @param throwable the throwable
     */
    public static void error(final Logger logger, final String msg, final Throwable throwable) {
        logger.error(msg, throwable);
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
        logger.warn(message, throwable);
    }

    /**
     * Get first non-null exception message, and return class name if all messages null.
     * @param throwable Top level throwable
     * @return String containing first non-null exception message, or Throwable simple class name
     */
    static String getMessage(final Throwable throwable) {
        if (StringUtils.isEmpty(throwable.getMessage())) {
            val message = ExceptionUtils.getThrowableList(throwable)
                    .stream().map(t -> t.getMessage()).filter(m -> m != null).findFirst();
            if (message.isPresent()) {
                return message.get();
            }
        }
        return StringUtils.defaultIfEmpty(throwable.getMessage(), throwable.getClass().getSimpleName());
    }

}
