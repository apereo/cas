package org.apereo.cas.adaptors.gauth;

import org.apereo.cas.services.AbstractMultifactorAuthenticationProvider;

/**
 * The authentication provider for google authenticator.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GoogleAuthenticatorMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {

    private static final long serialVersionUID = 4789727148634156909L;
    
    @Override
    protected boolean isAvailable() {
        return true;
    }
}
