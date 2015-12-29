package org.jasig.cas.authentication;

import javax.validation.constraints.NotNull;

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
     * @param authenticationTransaction    the authn attempt
     * @param authenticationContextBuilder the authentication context
     * @return the transaction manager
     * @throws AuthenticationException the authentication exception
     */
    AuthenticationTransactionManager handle(@NotNull AuthenticationTransaction authenticationTransaction,
                                            @NotNull AuthenticationContextBuilder authenticationContextBuilder)
                                            throws AuthenticationException;

    /**
     * Sets authentication manager.
     *
     * @param authenticationManager the authentication manager
     */
    void setAuthenticationManager(@NotNull AuthenticationManager authenticationManager);
}
