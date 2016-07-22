package org.apereo.cas.services;

/**
 * Exception thrown when a service attempts to proxy when it is not allowed to.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class UnauthorizedProxyingException extends UnauthorizedServiceException {
    /** The code description. */
    public static final String CODE = "UNAUTHORIZED_SERVICE_PROXY";

    /** The msg description. */
    public static final String MESSAGE = "Proxying is not allowed for registered service ";
    
    /**
     * Comment for {@code serialVersionUID}.
     */
    private static final long serialVersionUID = -7307803750894078575L;

    /**
     * Instantiates a new unauthorized proxying exception.
     */
    public UnauthorizedProxyingException() {
        super(CODE);
    }

    /**
     * Instantiates a new unauthorized proxying exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public UnauthorizedProxyingException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new unauthorized proxying exception.
     *
     * @param message the message
     */
    public UnauthorizedProxyingException(final String message) {
        super(message);
    }
}
