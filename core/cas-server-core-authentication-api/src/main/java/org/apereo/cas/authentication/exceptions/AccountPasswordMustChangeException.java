package org.apereo.cas.authentication.exceptions;

import lombok.extern.slf4j.Slf4j;
import javax.security.auth.login.CredentialExpiredException;
import lombok.NoArgsConstructor;

/**
 * Describes an authentication error condition where a user account's password must change before login.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@Slf4j
@NoArgsConstructor
public class AccountPasswordMustChangeException extends CredentialExpiredException {

    /** Serialization metadata. */
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
