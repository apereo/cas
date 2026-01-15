package org.apereo.cas.pac4j.client;

import module java.base;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link GroovyDelegatedClientIdentityProviderRedirectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiredArgsConstructor
@Slf4j
public class GroovyDelegatedClientIdentityProviderRedirectionStrategy implements DelegatedClientIdentityProviderRedirectionStrategy {
    private final ServicesManager servicesManager;

    private final ExecutableCompiledScript watchableScript;

    private final ApplicationContext applicationContext;

    @Override
    public Optional<DelegatedClientIdentityProviderConfiguration> select(
        final RequestContext context,
        final WebApplicationService service,
        final Set<DelegatedClientIdentityProviderConfiguration> providers) throws Throwable {
        val registeredService = servicesManager.findServiceBy(service);
        val args = new Object[]{context, service, registeredService, providers, applicationContext, LOGGER};
        return Optional.ofNullable(watchableScript.execute(args, DelegatedClientIdentityProviderConfiguration.class));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
