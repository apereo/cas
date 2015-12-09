package org.jasig.cas.authentication;

import org.jasig.cas.authentication.principal.Principal;

/**
 * The {@link AuthenticationContext} is an abstraction on top of a given authentication request.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public interface AuthenticationContext {

    /**
     * Obtains the primary authentication event for this context
     * @return the authentication
     */
    Authentication getAuthentication();

}
