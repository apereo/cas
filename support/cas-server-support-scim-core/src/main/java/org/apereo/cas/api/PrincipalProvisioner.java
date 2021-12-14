package org.apereo.cas.api;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.RegisteredService;

/**
 * This is {@link PrincipalProvisioner}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public interface PrincipalProvisioner {
    /**
     * Default implementation bean name.
     */
    String BEAN_NAME = "scimProvisioner";

    /**
     * Create principal.
     *
     * @param authentication    the authentication
     * @param credential        the credential
     * @param registeredService the registered service
     * @return true /false
     */
    boolean provision(Authentication authentication, Credential credential, RegisteredService registeredService);

    /**
     * Create boolean.
     *
     * @param principal  the principal
     * @param credential the credential
     * @return the boolean
     */
    boolean provision(Principal principal, Credential credential);
}
