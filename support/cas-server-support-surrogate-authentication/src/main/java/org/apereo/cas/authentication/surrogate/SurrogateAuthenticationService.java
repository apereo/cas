package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.principal.Principal;

import java.util.Collection;

/**
 * This is {@link SurrogateAuthenticationService}.
 * It defines operations to note whether one can substitute as another during authentication.
 *
 * @author Jonathan Johnson
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface SurrogateAuthenticationService {
    /**
     * Checks whether a surrogate can authenticate as a particular user.
     *
     * @param surrogate The username of the surrogate
     * @param principal the principal
     * @return true if the given surrogate can authenticate as the user
     */
    boolean canAuthenticateAs(String surrogate, Principal principal);

    /**
     * Gets a collection of account names a surrogate can authenticate as.
     *
     * @param username The username of the surrogate
     * @return collection of usernames
     */
    Collection<String> getEligibleAccountsForSurrogateToProxy(String username);
}
