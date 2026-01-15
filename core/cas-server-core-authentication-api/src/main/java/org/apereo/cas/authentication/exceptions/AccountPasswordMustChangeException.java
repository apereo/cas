package org.apereo.cas.authentication.exceptions;

import module java.base;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

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

    public AccountPasswordMustChangeException(@Nullable final String msg) {
        super(msg);
    }
}
