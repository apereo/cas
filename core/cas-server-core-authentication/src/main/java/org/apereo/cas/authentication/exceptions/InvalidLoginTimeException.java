package org.apereo.cas.authentication.exceptions;

import javax.security.auth.login.AccountException;

/**
 * Describes an error condition where authentication occurs at a time that is disallowed by security policy
 * applied to the underlying user account.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class InvalidLoginTimeException extends AccountException {

    private static final long serialVersionUID = -6699752791525619208L;

    /**
     * Instantiates a new invalid login time exception.
     */
    public InvalidLoginTimeException() {
        super();
    }

    /**
     * Instantiates a new invalid login time exception.
     *
     * @param message the message
     */
    public InvalidLoginTimeException(final String message) {
        super(message);
    }

}
