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
    protected static void throwException(final Throwable exception) {
        LoggingUtils.error(LOGGER, exception);
        throw exception instanceof final RuntimeException re ? re : new RuntimeException(exception);
    }
}
