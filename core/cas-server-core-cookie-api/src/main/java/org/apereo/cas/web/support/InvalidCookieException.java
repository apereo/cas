package org.apereo.cas.web.support;

/**
 * This exception is thrown when there are problems found with cookies.
 * @author Hal Deadman
 * @since 6.2.0
 */
public class InvalidCookieException extends RuntimeException {
    public InvalidCookieException(final String message) {
        super(message);
    }

    public InvalidCookieException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
