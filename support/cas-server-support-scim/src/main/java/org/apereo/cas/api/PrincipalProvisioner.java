package org.apereo.cas.api;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;

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
     * @param authentication the authentication
     * @param p              the principal
     * @param credential     the credential
     * @return true /false
     */
    boolean create(Authentication authentication, Principal p, Credential credential);
}
