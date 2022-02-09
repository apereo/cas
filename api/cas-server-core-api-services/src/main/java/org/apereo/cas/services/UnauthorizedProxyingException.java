package org.apereo.cas.services;


/**
 * Exception thrown when a service attempts to proxy when it is not allowed to.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class UnauthorizedProxyingException extends UnauthorizedServiceException {
    /**
     * The code description.
     */
    public static final String CODE = "UNAUTHORIZED_SERVICE_PROXY";

    /**
     * The msg description.
     */
    public static final String MESSAGE = "Proxying is not allowed for registered service ";

    private static final long serialVersionUID = -7307803750894078575L;

    public UnauthorizedProxyingException() {
        super(CODE);
    }

    public UnauthorizedProxyingException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedProxyingException(final String message) {
        super(message);
    }
}
