package org.apereo.cas.adaptors.azure;

import org.apereo.cas.services.AbstractMultifactorAuthenticationProvider;

/**
 * The authentication provider for azure authenticator.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class AzureAuthenticatorMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {

    private static final long serialVersionUID = 4789727148634156909L;
    
    @Override
    protected boolean isAvailable() {
        return true;
    }
}
