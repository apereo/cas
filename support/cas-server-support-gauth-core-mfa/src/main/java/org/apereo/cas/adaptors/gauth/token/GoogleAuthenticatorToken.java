package org.apereo.cas.adaptors.gauth.token;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@NoArgsConstructor
public class GoogleAuthenticatorToken extends OneTimeToken {
    private static final long serialVersionUID = 8494781829798273770L;

    public GoogleAuthenticatorToken(final Integer token, final String userId) {
        super(token, userId);
    }
}
