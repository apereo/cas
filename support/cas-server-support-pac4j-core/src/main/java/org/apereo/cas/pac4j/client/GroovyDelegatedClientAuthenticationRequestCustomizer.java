package org.apereo.cas.pac4j.client;

import module java.base;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.WebContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;

/**
 * This is {@link GroovyDelegatedClientAuthenticationRequestCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
@Slf4j
public class GroovyDelegatedClientAuthenticationRequestCustomizer implements DelegatedClientAuthenticationRequestCustomizer {
    private final ExecutableCompiledScript watchableScript;

    private final ApplicationContext applicationContext;

    @Override
    public void customize(final IndirectClient client, final WebContext webContext) throws Throwable {
        val args = new Object[]{client, webContext, applicationContext, LOGGER};
        watchableScript.execute(args, Void.class);
    }

    @Override
    public boolean supports(final IndirectClient client, final WebContext webContext) throws Throwable {
        val args = new Object[]{client, webContext, applicationContext, LOGGER};
        return watchableScript.execute("supports", Boolean.class, args);
    }

    @Override
    public boolean isAuthorized(final WebContext webContext, final IndirectClient client,
                                final WebApplicationService currentService) throws Throwable {
        val args = new Object[]{client, webContext, currentService, applicationContext, LOGGER};
        return watchableScript.execute("isAuthorized", Boolean.class, args);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
