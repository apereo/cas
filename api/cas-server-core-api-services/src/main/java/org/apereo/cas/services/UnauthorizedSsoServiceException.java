package org.apereo.cas.services;

import java.io.Serial;

/**
 * Exception thrown when a service attempts to use SSO when it should not be
 * allowed to.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class UnauthorizedSsoServiceException extends UnauthorizedServiceException {

    /**
     * The error code.
     */
    public static final String CODE = "service.not.authorized.sso";

    @Serial
    private static final long serialVersionUID = 8909291297815558561L;

    public UnauthorizedSsoServiceException() {
        this(CODE);
    }

    public UnauthorizedSsoServiceException(final String message, final Throwable cause) {
        super(cause, CODE, message);
    }

    public UnauthorizedSsoServiceException(final String message) {
        super(message);
    }

}
