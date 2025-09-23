package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServicePrincipalAccessStrategyEnforcer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.Collection;
import java.util.Optional;

/**
 * This is {@link GroovySurrogateAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class GroovySurrogateAuthenticationService extends BaseSurrogateAuthenticationService implements DisposableBean {
    private final ExecutableCompiledScript watchableScript;

    public GroovySurrogateAuthenticationService(final ServicesManager servicesManager,
                                                final CasConfigurationProperties casProperties,
                                                final RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer,
                                                final ConfigurableApplicationContext applicationContext) {
        super(servicesManager, casProperties, principalAccessStrategyEnforcer, applicationContext);
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        val groovyResource = casProperties.getAuthn().getSurrogate().getGroovy().getLocation();
        this.watchableScript = scriptFactory.fromResource(groovyResource);
    }

    @Override
    public boolean canImpersonateInternal(final String surrogate, final Principal principal, final Optional<? extends Service> service) throws Throwable {
        val args = new Object[]{surrogate, principal, service.orElse(null), LOGGER};
        return watchableScript.execute("canAuthenticate", Boolean.class, args);
    }

    @Override
    public Collection<String> getImpersonationAccounts(final String username, final Optional<? extends Service> service) throws Throwable {
        val args = new Object[]{username, service.orElse(null), LOGGER};
        return watchableScript.execute("getAccounts", Collection.class, args);
    }

    @Override
    public boolean isWildcardedAccount(final String surrogate, final Principal principal,
                                       final Optional<? extends Service> service) throws Throwable {
        val args = new Object[]{surrogate, principal, service.orElse(null), LOGGER};
        return super.isWildcardedAccount(surrogate, principal, service)
            && watchableScript.execute("isWildcardAuthorized", Boolean.class, args);
    }

    @Override
    public void destroy() {
        watchableScript.close();
    }
}
