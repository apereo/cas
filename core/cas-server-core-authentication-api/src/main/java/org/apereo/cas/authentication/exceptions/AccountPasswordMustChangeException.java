package org.apereo.cas.authentication.exceptions;

import lombok.NoArgsConstructor;

import javax.security.auth.login.CredentialExpiredException;

import java.io.Serial;

/**
 * Describes an authentication error condition where a user account's password must change before login.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@NoArgsConstructor
public class AccountPasswordMustChangeException extends CredentialExpiredException {

    @Serial
    private static final long serialVersionUID = 7487835035108753209L;

    /**
     * Instantiates a new account password must change exception.
     *
     * @param msg the msg
     */
    public AccountPasswordMustChangeException(final String msg) {
        super(msg);
    }
}
