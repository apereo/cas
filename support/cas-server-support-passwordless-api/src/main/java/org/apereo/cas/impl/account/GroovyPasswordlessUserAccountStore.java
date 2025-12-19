package org.apereo.cas.impl.account;

import module java.base;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link GroovyPasswordlessUserAccountStore}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class GroovyPasswordlessUserAccountStore implements PasswordlessUserAccountStore {
    protected final ExecutableCompiledScript watchableScript;
    protected final ConfigurableApplicationContext applicationContext;

    @Override
    public Optional<PasswordlessUserAccount> findUser(final PasswordlessAuthenticationRequest request) throws Throwable {
        val args = new Object[]{request, LOGGER};
        return Optional.ofNullable(watchableScript.execute(args, PasswordlessUserAccount.class));
    }
}
