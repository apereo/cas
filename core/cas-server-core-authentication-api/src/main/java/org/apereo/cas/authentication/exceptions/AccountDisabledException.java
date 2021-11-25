package org.apereo.cas.authentication.exceptions;

import lombok.NoArgsConstructor;

import javax.security.auth.login.AccountException;

/**
 * Describes an authentication error condition where a user account has been administratively disabled.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@NoArgsConstructor
public class AccountDisabledException extends AccountException {

    private static final long serialVersionUID = 7487835035108753209L;

    public AccountDisabledException(final String msg) {
        super(msg);
    }
}
