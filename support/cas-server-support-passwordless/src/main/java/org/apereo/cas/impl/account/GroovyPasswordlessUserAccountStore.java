package org.apereo.cas.impl.account;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;

import java.util.Optional;

/**
 * This is {@link GroovyPasswordlessUserAccountStore}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class GroovyPasswordlessUserAccountStore implements PasswordlessUserAccountStore {
    @Override
    public Optional<PasswordlessUserAccount> findUser(final String username) {
        return null;
    }
}
