package org.apereo.cas.adaptors.authy;

import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;

/**
 * The authentication provider for google authenticator.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class AuthyMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {
    private static final long serialVersionUID = 4789727148634156909L;

    @Override
    public String getFriendlyName() {
        return "Authy";
    }
}
