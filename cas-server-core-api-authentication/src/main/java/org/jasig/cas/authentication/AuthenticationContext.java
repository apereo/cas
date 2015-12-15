package org.jasig.cas.authentication;

import org.jasig.cas.authentication.principal.Service;

/**
 * The {@link AuthenticationContext} is an abstraction on top of a given authentication request.
 * An authentication context carries the primary and composite authentication event, collected
 * from all authentication attempts. The principal and attributes associated with this authentication
 * are collected out of all events.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public interface AuthenticationContext {

    /**
     * Obtains the primary authentication event for this context.
     * @return the authentication
     */
    Authentication getAuthentication();

    /**
     * Gets the service for which this authentication context is relevant.
     * The service MAY be null, as an authentication context in CAS
     * can be established without providing a service/destination.
     * @return the service
     */
    Service getService();
}
