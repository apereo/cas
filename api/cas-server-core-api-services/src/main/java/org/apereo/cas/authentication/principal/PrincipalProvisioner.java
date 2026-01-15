package org.apereo.cas.authentication.principal;

import module java.base;
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
     * Default implementation bean name.
     */
    String BEAN_NAME = "principalProvisioner";

    /**
     * Create principal.
     *
     * @param authentication    the authentication
     * @param credential        the credential
     * @param registeredService the registered service
     * @return true /false
     */
    default boolean provision(final Authentication authentication, final Credential credential,
                              final RegisteredService registeredService) {
        return provision(authentication.getPrincipal(), credential);
    }

    /**
     * Create and provision.
     *
     * @param principal  the principal
     * @param credential the credential
     * @return true/false
     */
    boolean provision(Principal principal, Credential credential);
}
