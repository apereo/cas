package org.apereo.cas.web.flow;

import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.RequestContext;
import java.util.Set;

/**
 * This is {@link DelegatedClientIdentityProviderConfigurationGroovyPostProcessor}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
@Slf4j
public class DelegatedClientIdentityProviderConfigurationGroovyPostProcessor
    implements DelegatedClientIdentityProviderConfigurationPostProcessor {
    private final ExecutableCompiledScript watchableScript;

    @Override
    public void process(final RequestContext context,
                        final Set<DelegatedClientIdentityProviderConfiguration> providers) throws Throwable {
        val args = new Object[]{context, providers, LOGGER};
        watchableScript.execute(args, Void.class);
    }
}
