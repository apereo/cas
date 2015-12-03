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
     * Total number of active authentications in this context
     * @return total count of authentications
     */
    int count();

    /**
     * Determines whether the context is empty.
     * A non-empty context must contain a primary authentication and principal.
     * @return true if context is empty.
     */
    boolean isEmpty();

    /**
     * Collect a new authenication event and store it.
     * @param authentication the new authentication event
     */
    boolean collect(Authentication authentication);

    /**
     * Obtain the primary principal for this authentication context.
     *
     * @return primary principal
     */
    Principal getPrimaryPrincipal();

    /**
     * Obtains the primary authentication event for this context
     * @return the authentication
     */
    Authentication getPrimaryAuthentication();

}
