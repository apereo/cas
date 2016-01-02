package org.jasig.cas.services;

import java.io.Serializable;

/**
 * This is {@link RegisteredServiceAuthenticationPolicy} that describes how a service
 * should handle authentication requests.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public interface RegisteredServiceAuthenticationPolicy extends Serializable {

    /**
     * Gets authentication method.
     *
     * @return the authentication method
     */
    String getAuthenticationMethod();
}
