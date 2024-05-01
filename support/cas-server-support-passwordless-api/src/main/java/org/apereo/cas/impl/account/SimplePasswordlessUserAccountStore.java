package org.apereo.cas.impl.account;

import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.util.RegexUtils;
import lombok.Getter;
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
@Getter
public class SimplePasswordlessUserAccountStore implements PasswordlessUserAccountStore {
    /**
     * Map of all passwordless accounts read from resources.
     */
    protected final Map<String, PasswordlessUserAccount> accounts;

    @Override
    public Optional<PasswordlessUserAccount> findUser(final PasswordlessAuthenticationRequest request) {
        return accounts
            .entrySet()
            .stream()
            .filter(entry -> RegexUtils.find(entry.getKey(), request.getUsername()))
            .map(Map.Entry::getValue)
            .findFirst();
    }
}
