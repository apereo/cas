package org.apereo.cas.authentication.exceptions;

import javax.security.auth.login.AccountException;

/**
 * Describes an authentication error condition where a user account has been administratively disabled.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class AccountDisabledException extends AccountException {

    /** Serialization metadata. */
    private static final long serialVersionUID = 7487835035108753209L;

    /**
     * Instantiates a new account disabled exception.
     */
    public AccountDisabledException() {
    }

    /**
     * Instantiates a new account disabled exception.
     *
     * @param msg the msg
     */
    public AccountDisabledException(final String msg) {
        super(msg);
    }
}
