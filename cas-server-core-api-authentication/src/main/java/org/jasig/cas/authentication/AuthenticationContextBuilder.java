package org.jasig.cas.authentication;

/**
 * This is {@link AuthenticationContextBuilder}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public interface AuthenticationContextBuilder {

    /**
     * Collect authentication context builder.
     *
     * @param authentication the authentication
     * @return the authentication context builder
     */
    AuthenticationContextBuilder collect(final Authentication authentication);

    /**
     * Build authentication context.
     *
     * @return the authentication context
     */
    AuthenticationContext build();
}
