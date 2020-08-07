package org.apereo.cas.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
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
        if (logger.isDebugEnabled()) {
            logger.error(msg, throwable);
        } else {
            logger.error(msg);
        }
    }

    /**
     * Log Error.
     *
     * @param logger    the logger
     * @param throwable the throwable
     */
    public void error(final Logger logger, final Throwable throwable) {
        error(logger, StringUtils.defaultIfEmpty(throwable.getMessage(), throwable.getClass().getSimpleName()), throwable);
    }

    /**
     * Log warning.
     *
     * @param logger    the logger
     * @param throwable the throwable
     */
    public void warn(final Logger logger, final Throwable throwable) {
        if (logger.isDebugEnabled()) {
            logger.warn(throwable.getMessage(), throwable);
        } else {
            logger.warn(throwable.getMessage());
        }
    }

    /**
     * Log warning.
     *
     * @param logger    the logger
     * @param message   the message
     * @param throwable the throwable
     */
    public void warn(final Logger logger, final String message, final Throwable throwable) {
        if (logger.isDebugEnabled()) {
            logger.warn(message, throwable);
        } else {
            logger.warn(message);
        }
    }

}
