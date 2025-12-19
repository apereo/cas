package org.apereo.cas.gauth.token;

import module java.base;
import org.apereo.cas.authentication.OneTimeToken;
import lombok.NoArgsConstructor;

/**
 * This is {@link GoogleAuthenticatorToken}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@NoArgsConstructor
public class GoogleAuthenticatorToken extends OneTimeToken {
    @Serial
    private static final long serialVersionUID = 8494781829798273770L;

    public GoogleAuthenticatorToken(final Integer token, final String userId) {
        super(token, userId);
    }
}
