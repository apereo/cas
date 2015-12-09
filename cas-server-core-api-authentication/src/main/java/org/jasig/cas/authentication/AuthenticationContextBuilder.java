package org.jasig.cas.authentication;

/**
 * This is {@link AuthenticationContextBuilder}. Builds an authentication context,
 * and collects authentication events to form a line of history from which the primary
 * composed context can be gleaned.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public interface AuthenticationContextBuilder {
    /**
     * Total number of active authentications in this context.
     *
     * @return total count of authentications
     */
    int count();

    /**
     * Determines whether the context is empty.
     * A non-empty context must contain a primary authentication and principal.
     *
     * @return true if context is empty.
     */
    boolean isEmpty();

    /**
     * Collect a new authenication event and store it.
     *
     * @param authentication the new authentication event
     * @return the boolean
     * @throws AuthenticationException the authentication exception
     */
    boolean collect(Authentication authentication) throws AuthenticationException;

    /**
     * Build authentication context.
     *
     * @return the authentication context
     */
    AuthenticationContext build();

    /**
     * Clear.
     */
    void clear();
}
