package org.apereo.cas.impl.account;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.util.ScriptingUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
        val account = ScriptingUtils.executeGroovyScript(groovyResource,
            new Object[]{username, LOGGER}, PasswordlessUserAccount.class, true);
        return Optional.ofNullable(account);
    }
}
