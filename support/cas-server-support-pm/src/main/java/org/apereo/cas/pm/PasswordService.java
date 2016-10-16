package org.apereo.cas.pm;

import org.apereo.cas.authentication.Credential;

/**
 * This is {@link PasswordService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface PasswordService {

    /**
     * Execute op to change password.
     *
     * @param c    the credentials
     * @param bean the bean
     * @return true /false
     */
    default boolean change(Credential c, PasswordChangeBean bean) { return false; }

    /**
     * Find email associated with username.
     *
     * @param username the username
     * @return the string
     */
    default String findEmail(String username) { return null; }
    
    /**
     * Track token issued for password reset.
     *
     * @param username the username
     * @param token    the token
     */
    default void trackToken(String username, String token) {}

    default String createResetUrl(String token) { return null; }

    default String createToken() { return null; }
}
