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
    public static final String THROW_ON_ERROR = "throwOnError";

    /**
     * Throw fatal errors if set.
     */
    private static boolean THROW_ON_ERRORS;

    public static boolean isThrowOnErrors() {
        return THROW_ON_ERRORS;
    }

    public static void setThrowOnErrors(final boolean throwOnErrors) {
        THROW_ON_ERRORS = throwOnErrors;
    }

    protected static void logException(final Exception e) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.error(e.getMessage(), e);
        } else {
            LOGGER.error(e.getMessage());
        }
        if (isThrowOnErrors()) {
            throw new RuntimeException(e);
        }
    }

}
