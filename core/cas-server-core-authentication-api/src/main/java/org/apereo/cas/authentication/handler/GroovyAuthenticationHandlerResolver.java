package org.apereo.cas.authentication.handler;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandlerResolver;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;

import java.util.Set;

/**
 * This is {@link GroovyAuthenticationHandlerResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Getter
@Setter
public class GroovyAuthenticationHandlerResolver implements AuthenticationHandlerResolver, DisposableBean {
    private final WatchableGroovyScriptResource watchableScript;

    private final ServicesManager servicesManager;

    private int order;

    public GroovyAuthenticationHandlerResolver(final Resource groovyResource, final ServicesManager servicesManager) {
        this(groovyResource, servicesManager, Ordered.LOWEST_PRECEDENCE);
    }

    public GroovyAuthenticationHandlerResolver(final Resource groovyResource, final ServicesManager servicesManager, final int order) {
        this.order = order;
        this.servicesManager = servicesManager;
        this.watchableScript = new WatchableGroovyScriptResource(groovyResource);
    }

    @Override
    public Set<AuthenticationHandler> resolve(final Set<AuthenticationHandler> candidateHandlers, final AuthenticationTransaction transaction) {
        val args = new Object[]{candidateHandlers, transaction, servicesManager, LOGGER};
        return watchableScript.execute(args, Set.class);
    }

    @Override

    public boolean supports(final Set<AuthenticationHandler> handlers, final AuthenticationTransaction transaction) {
        val args = new Object[]{handlers, transaction, servicesManager, LOGGER};
        return watchableScript.execute("supports", Boolean.class, args);
    }

    @Override
    public void destroy() {
        this.watchableScript.close();
    }
}
