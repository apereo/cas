package org.jasig.cas.services;

import java.io.Serializable;
import java.util.Set;

/**
 * This is {@link RegisteredServiceAuthenticationPolicy} that describes how a service
 * should handle authentication requests.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public interface RegisteredServiceAuthenticationPolicy extends Serializable {

    /**
     * Gets MFA authentication provider id.
     *
     * @return the authentication provider id
     */
    Set<String> getMultifactorAuthenticationProviders();

    /**
     * Is fail open boolean.
     *
     * @return the boolean
     */
    boolean isFailOpen();

    /**
     * Gets principal attribute name trigger.
     *
     * @return the principal attribute name trigger
     */
    String getPrincipalAttributeNameTrigger();

    /**
     * Gets principal attribute value to match.
     * Values may be regex patterns.
     *
     * @return the principal attribute value to match
     */
    String getPrincipalAttributeValueToMatch();

}
