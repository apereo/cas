package org.apereo.cas.services;

/**
 * Exception thrown when a service attempts to use SSO when it should not be
 * allowed to.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class UnauthorizedSsoServiceException extends UnauthorizedServiceException {

    /**
     * Comment for {@code serialVersionUID}.
     */
    private static final long serialVersionUID = 8909291297815558561L;

    /** The code description. */
    private static final String CODE = "service.not.authorized.sso";

    /**
     * Instantiates a new unauthorized sso service exception.
     */
    public UnauthorizedSsoServiceException() {
        this(CODE);
    }

    /**
     * Instantiates a new unauthorized sso service exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public UnauthorizedSsoServiceException(final String message,
        final Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new unauthorized sso service exception.
     *
     * @param message the message
     */
    public UnauthorizedSsoServiceException(final String message) {
        super(message);
    }

}
