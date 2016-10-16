package org.apereo.cas.pm;

import org.apereo.cas.authentication.Credential;

/**
 * This is {@link PasswordManagementService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface PasswordManagementService {

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
    
    default String createToken(final String username) { return null; }
}
