package org.apereo.cas.pac4j.client;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * This is {@link GroovyDelegatedClientIdentityProviderRedirectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiredArgsConstructor
@Slf4j
public class GroovyDelegatedClientIdentityProviderRedirectionStrategy implements DelegatedClientIdentityProviderRedirectionStrategy {
    /**
     * The Services manager.
     */
    private final ServicesManager servicesManager;

    private final WatchableGroovyScriptResource watchableScript;

    @Override
    public Optional<DelegatedClientIdentityProviderConfiguration> getPrimaryDelegatedAuthenticationProvider(final RequestContext context,
                                                                                                            final WebApplicationService service,
                                                                                                            final DelegatedClientIdentityProviderConfiguration provider) {
        val registeredService = servicesManager.findServiceBy(service);
        val args = new Object[]{context, service, registeredService, provider, LOGGER};
        return Optional.ofNullable(watchableScript.execute(args, DelegatedClientIdentityProviderConfiguration.class));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
