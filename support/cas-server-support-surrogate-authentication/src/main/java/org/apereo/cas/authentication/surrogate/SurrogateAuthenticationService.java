package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;

import java.util.Collection;

/**
 * This is {@link SurrogateAuthenticationService}.
 * It defines operations to note whether one can substitute as another during authentication.
 *
 * @author Jonathan Johnson
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@FunctionalInterface
public interface SurrogateAuthenticationService {
    /**
     * Surrogate username attribute in the authentication payload.
     */
    String AUTHENTICATION_ATTR_SURROGATE_USER = "surrogateUser";
    /**
     * Original credential attribute in the authentication payload.
     */
    String AUTHENTICATION_ATTR_SURROGATE_PRINCIPAL = "surrogatePrincipal";
    /**
     * Indicates that surrogate authn is enabled and activated.
     */
    String AUTHENTICATION_ATTR_SURROGATE_ENABLED = "surrogateEnabled";
    
    /**
     * Checks whether a principal can authenticate as a surrogate user.
     *
     * @param surrogate The username of the surrogate
     * @param principal the principal
     * @param service   the service
     * @return true if the given surrogate can authenticate as the user
     */
    default boolean canAuthenticateAs(final String surrogate, final Principal principal, final Service service) {
        return false;
    }

    /**
     * Gets a collection of account names a surrogate can authenticate as.
     *
     * @param username The username of the surrogate
     * @return collection of usernames
     */
    Collection<String> getEligibleAccountsForSurrogateToProxy(String username);
}
