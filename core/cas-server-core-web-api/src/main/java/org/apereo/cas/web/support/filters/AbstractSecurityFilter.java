package org.apereo.cas.web.support.filters;

import org.apereo.cas.util.LoggingUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link AbstractSecurityFilter}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@Setter
@Getter
public abstract class AbstractSecurityFilter {
    /**
     * Log exception.
     *
     * @param e the exception
     */
    protected static void logException(final Exception e) {
        LoggingUtils.error(LOGGER, e);
        throw new RuntimeException(e);
    }

}
