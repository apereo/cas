package org.jasig.cas.authentication;

/**
 * This is {@link AuthenticationContextBuilder}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public interface AuthenticationContextBuilder {
    /**
     * Total number of active authentications in this context
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
     */
    boolean collect(Authentication authentication);

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
