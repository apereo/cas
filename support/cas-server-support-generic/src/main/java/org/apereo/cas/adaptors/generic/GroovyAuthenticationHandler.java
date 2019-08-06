package org.apereo.cas.adaptors.generic;

import org.apereo.cas.authentication.AbstractAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.Resource;

/**
 * This is {@link GroovyAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class GroovyAuthenticationHandler extends AbstractAuthenticationHandler {
    private final transient WatchableGroovyScriptResource watchableScript;

    public GroovyAuthenticationHandler(final String name,
                                       final ServicesManager servicesManager,
                                       final PrincipalFactory principalFactory,
                                       final Resource groovyResource,
                                       final Integer order) {
        super(name, servicesManager, principalFactory, order);
        this.watchableScript = new WatchableGroovyScriptResource(groovyResource);
    }

    @Override
    public AuthenticationHandlerExecutionResult authenticate(final Credential credential) {
        val args = new Object[]{this, credential, getServicesManager(), getPrincipalFactory(), LOGGER};
        return watchableScript.execute("authenticate", AuthenticationHandlerExecutionResult.class, args);
    }

    @Override
    public boolean supports(final Credential credential) {
        val args = new Object[]{credential, LOGGER};
        return watchableScript.execute("supportsCredential", Boolean.class, args);
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        val args = new Object[]{clazz, LOGGER};
        return watchableScript.execute("supportsCredentialClass", Boolean.class, args);
    }
}
