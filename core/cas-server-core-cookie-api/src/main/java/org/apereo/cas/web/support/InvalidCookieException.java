package org.apereo.cas.web.support;

import org.apereo.cas.authentication.RootCasException;

import java.io.Serial;

/**
 * This exception is thrown when there are problems found with cookies.
 *
 * @author Hal Deadman
 * @since 6.2.0
 */
public class InvalidCookieException extends RootCasException {
    @Serial
    private static final long serialVersionUID = -994393142011101111L;

    public InvalidCookieException(final String message) {
        super(message);
    }
}
