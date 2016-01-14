package org.jasig.cas.services;

import org.jasig.cas.authentication.AuthenticationException;

import java.io.Serializable;

/**
 * This is {@link RegisteredServiceMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public interface RegisteredServiceMultifactorAuthenticationProvider extends Serializable {

    /**
     * Provide string.
     *
     * @param service the service
     * @return the string
     * @throws AuthenticationException the authentication exception
     */
    String provide(RegisteredService service) throws AuthenticationException;
}
