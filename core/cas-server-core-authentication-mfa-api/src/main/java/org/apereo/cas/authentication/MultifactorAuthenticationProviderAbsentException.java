package org.apereo.cas.authentication;

import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * This is {@link MultifactorAuthenticationProviderAbsentException}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@NoArgsConstructor
public class MultifactorAuthenticationProviderAbsentException extends AuthenticationException {
    @Serial
    private static final long serialVersionUID = 5909155188008680032L;

    public MultifactorAuthenticationProviderAbsentException(final String msg) {
        super(msg);
    }

}
