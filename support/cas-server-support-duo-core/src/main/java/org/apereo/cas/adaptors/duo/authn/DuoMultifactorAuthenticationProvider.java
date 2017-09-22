package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.services.MultifactorAuthenticationProvider;

/**
 * This is {@link DuoMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface DuoMultifactorAuthenticationProvider extends MultifactorAuthenticationProvider {

    /**
     * Gets duo authentication service.
     *
     * @return the duo authentication service
     */
    DuoAuthenticationService getDuoAuthenticationService();
}
