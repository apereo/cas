package org.apereo.cas.services;


/**
 * Exception thrown when a service attempts to use SSO when it should not be
 * allowed to.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class UnauthorizedSsoServiceException extends UnauthorizedServiceException {

    private static final long serialVersionUID = 8909291297815558561L;

    /**
     * The code description.
     */
    private static final String CODE = "service.not.authorized.sso";

    public UnauthorizedSsoServiceException() {
        this(CODE);
    }

    public UnauthorizedSsoServiceException(final String message,
                                           final Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedSsoServiceException(final String message) {
        super(message);
    }

}
