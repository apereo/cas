package org.apereo.cas.web.support.filters;

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
     * The name of the optional Filter init-param specifying that configuration
     * errors should be fatal.
     */
    public static final String FAIL_SAFE = "failSafe";

    /**
     * Throw fatal errors if set.
     */
    public static boolean throwOnErrors;

    protected static void logException(final Exception e) {
        LOGGER.error(e.getMessage(), e);
        if (throwOnErrors) {
            throw new RuntimeException(e);
        }
    }

}
