package org.jasig.cas.services;

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
     * @param policy the policy
     * @return the string
     */
    String provide(RegisteredServiceAuthenticationPolicy policy);
}
