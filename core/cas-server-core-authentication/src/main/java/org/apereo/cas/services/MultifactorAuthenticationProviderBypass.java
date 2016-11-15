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
     * @param registeredService the registered service in question
     * @return false is request isn't supported and can be bypassed. true otherwise.
     */
    boolean isAuthenticationRequestHonored(Authentication authentication, RegisteredService registeredService);
}
