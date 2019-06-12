package org.apereo.cas.impl.account;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

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

    private final transient WatchableGroovyScriptResource watchableScript;

    public GroovyPasswordlessUserAccountStore(final Resource groovyScript) {
        this.watchableScript = new WatchableGroovyScriptResource(groovyScript);
    }

    @Override
    public Optional<PasswordlessUserAccount> findUser(final String username) {
        val args = new Object[]{username, LOGGER};
        return Optional.ofNullable(watchableScript.execute(args, PasswordlessUserAccount.class));
    }
}
