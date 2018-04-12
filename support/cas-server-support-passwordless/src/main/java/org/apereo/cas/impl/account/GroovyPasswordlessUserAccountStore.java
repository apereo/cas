package org.apereo.cas.impl.account;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.util.ScriptingUtils;
import org.springframework.core.io.Resource;

import java.util.Optional;

/**
 * This is {@link GroovyPasswordlessUserAccountStore}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class GroovyPasswordlessUserAccountStore implements PasswordlessUserAccountStore {
    private final transient Resource groovyResource;

    @Override
    public Optional<PasswordlessUserAccount> findUser(final String username) {
        final var account = ScriptingUtils.executeGroovyScript(groovyResource,
            new Object[]{username, LOGGER}, PasswordlessUserAccount.class);
        return Optional.ofNullable(account);
    }
}
