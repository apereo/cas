package org.apereo.cas.acct.provision;

import org.apereo.cas.acct.AccountRegistrationRequest;
import org.apereo.cas.acct.AccountRegistrationResponse;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;

/**
 * This is {@link GroovyAccountRegistrationProvisioner}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Slf4j
@RequiredArgsConstructor
public class GroovyAccountRegistrationProvisioner implements AccountRegistrationProvisioner {
    private final WatchableGroovyScriptResource watchableScript;

    private final ApplicationContext applicationContext;

    @Override
    public AccountRegistrationResponse provision(final AccountRegistrationRequest request) throws Exception {
        val args = new Object[]{request, applicationContext, LOGGER};
        return watchableScript.execute(args, AccountRegistrationResponse.class);
    }
}
