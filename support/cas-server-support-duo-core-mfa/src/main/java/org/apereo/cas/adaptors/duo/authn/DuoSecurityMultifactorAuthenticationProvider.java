package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationRegistrationProperties;

/**
 * This is {@link DuoSecurityMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface DuoSecurityMultifactorAuthenticationProvider extends MultifactorAuthenticationProvider {

    /**
     * Gets duo authentication service.
     *
     * @return the duo authentication service
     */
    DuoSecurityAuthenticationService getDuoAuthenticationService();

    /**
     * Gets registration settings for this provider.
     *
     * @return the registration
     */
    DuoSecurityMultifactorAuthenticationRegistrationProperties getRegistration();
}
