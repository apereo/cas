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
     * Default implementation bean name.
     */
    String BEAN_NAME = "passwordlessUserAccountStore";

    /**
     * Find user passwordless user account.
     *
     * @param username the username
     * @return the passwordless user account
     * @throws Throwable the throwable
     */
    Optional<PasswordlessUserAccount> findUser(String username) throws Throwable;

    /**
     * Load.
     */
    default void reload() {
    }
}
