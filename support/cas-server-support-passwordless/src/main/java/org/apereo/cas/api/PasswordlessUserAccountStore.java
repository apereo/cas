package org.apereo.cas.api;

import java.util.Optional;

/**
 * This is {@link PasswordlessUserAccountStore}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@FunctionalInterface
public interface PasswordlessUserAccountStore {

    /**
     * Find user passwordless user account.
     *
     * @param username the username
     * @return the passwordless user account
     */
    Optional<PasswordlessUserAccount> findUser(String username);
}
