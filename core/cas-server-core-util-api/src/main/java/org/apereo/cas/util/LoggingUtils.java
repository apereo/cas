package org.apereo.cas.util;

import lombok.experimental.UtilityClass;
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
     * Log Error.
     *
     * @param logger    the logger
     * @param throwable the throwable
     */
    public void error(final Logger logger, final Throwable throwable) {
        if (logger.isDebugEnabled()) {
            logger.error(throwable.getMessage(), throwable);
        } else {
            logger.error(throwable.getMessage());
        }
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
}
