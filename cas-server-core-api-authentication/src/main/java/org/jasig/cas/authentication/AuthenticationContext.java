package org.jasig.cas.authentication;

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

}
