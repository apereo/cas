package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.Resource;

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
    private final WatchableGroovyScriptResource watchableScript;

    public GroovySurrogateAuthenticationService(final ServicesManager servicesManager,
                                                final Resource groovyResource) {
        super(servicesManager);
        this.watchableScript = new WatchableGroovyScriptResource(groovyResource);
    }

    @Override
    public boolean canImpersonateInternal(final String surrogate, final Principal principal, final Optional<Service> service) {
        val args = new Object[]{surrogate, principal, service.orElse(null), LOGGER};
        return watchableScript.execute("canAuthenticate", Boolean.class, args);
    }

    @Override
    public Collection<String> getImpersonationAccounts(final String username) {
        val args = new Object[]{username, LOGGER};
        return watchableScript.execute("getAccounts", Collection.class, args);
    }

    @Override
    public boolean isWildcardedAccount(final String surrogate, final Principal principal) {
        val args = new Object[]{surrogate, principal, LOGGER};
        return super.isWildcardedAccount(surrogate, principal)
               && watchableScript.execute("isWildcardAuthorized", Boolean.class, args);
    }

    @Override
    public void destroy() {
        watchableScript.close();
    }
}
