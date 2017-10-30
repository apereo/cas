package org.apereo.cas.adaptors.gauth.token;

import org.apereo.cas.otp.repository.token.OneTimeToken;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * This is {@link GoogleAuthenticatorToken}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Entity
@Table(name = "GoogleAuthenticatorToken")
public class GoogleAuthenticatorToken extends OneTimeToken {
    private static final long serialVersionUID = 8494781829798273770L;

    public GoogleAuthenticatorToken() {
        super();
    }

    public GoogleAuthenticatorToken(final Integer token, final String userId) {
        super(token, userId);
    }
}
