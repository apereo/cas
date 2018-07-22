package org.apereo.cas.impl.account;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

/**
 * This is {@link SimplePasswordlessUserAccountStore}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
public class SimplePasswordlessUserAccountStore implements PasswordlessUserAccountStore {
    private final Map<String, PasswordlessUserAccount> accounts;

    @Override
    public Optional<PasswordlessUserAccount> findUser(final String username) {
        if (accounts.containsKey(username)) {
            return Optional.of(accounts.get(username));
        }
        return Optional.empty();
    }
}
