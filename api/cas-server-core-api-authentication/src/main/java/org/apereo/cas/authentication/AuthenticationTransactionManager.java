package org.apereo.cas.authentication;

/**
 * The {@link AuthenticationTransactionManager} deals exclusively with authentication concepts
 * e.g. Credentials, Principals, producing valid Authentication objects. It is invoked repeatedly with distinct credential type(s)
 * for interactive multi-staged authn flows that would authenticate at each step as opposed
 * to gather all credentials and send them for authentication in one batch.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public interface AuthenticationTransactionManager {
    /**
     * Handle authentication transaction manager.
     *
     * @param authenticationTransaction   the authn attempt
     * @param authenticationResultBuilder the authentication context
     * @return the transaction manager
     * @throws AuthenticationException the authentication exception
     */
    AuthenticationTransactionManager handle(AuthenticationTransaction authenticationTransaction,
                                            AuthenticationResultBuilder authenticationResultBuilder)
                                            throws AuthenticationException;

    /**
     * Gets authentication manager.
     *
     * @return the authentication manager
     */
    AuthenticationManager getAuthenticationManager();
}
