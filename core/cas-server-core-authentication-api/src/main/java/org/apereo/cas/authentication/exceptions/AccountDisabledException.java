package org.apereo.cas.authentication.exceptions;

import lombok.extern.slf4j.Slf4j;
import javax.security.auth.login.AccountException;
import lombok.NoArgsConstructor;

/**
 * Describes an authentication error condition where a user account has been administratively disabled.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
@NoArgsConstructor
public class AccountDisabledException extends AccountException {

    /** Serialization metadata. */
    private static final long serialVersionUID = 7487835035108753209L;

    /**
     * Instantiates a new account disabled exception.
     *
     * @param msg the msg
     */
    public AccountDisabledException(final String msg) {
        super(msg);
    }
}
