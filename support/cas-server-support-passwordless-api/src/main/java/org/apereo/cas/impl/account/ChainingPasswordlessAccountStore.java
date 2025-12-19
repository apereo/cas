package org.apereo.cas.impl.account;

import module java.base;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link ChainingPasswordlessAccountStore}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
public class ChainingPasswordlessAccountStore implements PasswordlessUserAccountStore {
    private final List<PasswordlessUserAccountStore> stores;

    @Override
    public Optional<? extends PasswordlessUserAccount> findUser(final PasswordlessAuthenticationRequest request) throws Throwable {
        for (val store : stores) {
            val user = store.findUser(request);
            if (user.isPresent()) {
                return user;
            }
        }
        return Optional.empty();
    }

    @Override
    public void reload() {
        stores.forEach(PasswordlessUserAccountStore::reload);
    }
}
