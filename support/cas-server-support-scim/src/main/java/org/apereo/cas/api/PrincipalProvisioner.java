package org.apereo.cas.api;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.services.RegisteredService;

/**
 * This is {@link PrincipalProvisioner}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@FunctionalInterface
public interface PrincipalProvisioner {

    /**
     * Create principal.
     *
     * @param authentication    the authentication
     * @param credential        the credential
     * @param registeredService the registered service
     * @return true /false
     */
    boolean create(Authentication authentication, Credential credential, RegisteredService registeredService);
}
