package org.apereo.cas.authentication.exceptions;

import javax.security.auth.login.AccountException;

/**
 * Describes an error condition where authentication occurs from a location that is disallowed by security policy
 * applied to the underlying user account.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class InvalidLoginLocationException extends AccountException {

    private static final long serialVersionUID = 5745711263227480194L;

    /**
     * Instantiates a new invalid login location exception.
     */
    public InvalidLoginLocationException() {
        super();
    }

    /**
     * Instantiates a new invalid login location exception.
     *
     * @param message the message
     */
    public InvalidLoginLocationException(final String message) {
        super(message);
    }
}
