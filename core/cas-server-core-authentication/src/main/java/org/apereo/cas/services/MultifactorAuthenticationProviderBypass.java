package org.apereo.cas.services;

import org.apereo.cas.authentication.Authentication;

import java.io.Serializable;

/**
 * This is {@link MultifactorAuthenticationProviderBypass}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface MultifactorAuthenticationProviderBypass extends Serializable {
    /**
     * Eval current bypass rules for the provider.
     *
     * @param authentication the authentication
     * @return false is request isn't supported and can be bypassed. true otherwise.
     */
    boolean eval(Authentication authentication);
}
