package org.apereo.cas.authentication;

import java.io.Serial;

/**
 * This is {@link MultifactorAuthenticationFailedException}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public class MultifactorAuthenticationFailedException extends AuthenticationException {
    @Serial
    private static final long serialVersionUID = 5909155188558680032L;

    private static final String CODE = "MULTIFACTOR_AUTHN_FAILED";

    public MultifactorAuthenticationFailedException(final Throwable cause) {
        super(cause);
    }

    @Override
    public String getCode() {
        return CODE;
    }
}
