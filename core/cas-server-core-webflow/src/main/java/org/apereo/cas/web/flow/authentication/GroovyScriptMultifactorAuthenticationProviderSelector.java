package org.apereo.cas.web.flow.authentication;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.ScriptingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.util.Collection;

/**
 * This is {@link GroovyScriptMultifactorAuthenticationProviderSelector}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class GroovyScriptMultifactorAuthenticationProviderSelector implements MultifactorAuthenticationProviderSelector {
    private static final Logger LOGGER = LoggerFactory.getLogger(RankedMultifactorAuthenticationProviderSelector.class);

    private final Resource groovyScript;

    public GroovyScriptMultifactorAuthenticationProviderSelector(final Resource groovyScript) {
        this.groovyScript = groovyScript;
    }

    @Override
    public MultifactorAuthenticationProvider resolve(final Collection<MultifactorAuthenticationProvider> providers,
                                                     final RegisteredService service, final Principal principal) {
        final Object[] args = {service, principal, providers, LOGGER};
        final String provider = ScriptingUtils.executeGroovyScript(groovyScript, args, String.class);
        if (StringUtils.isBlank(provider)) {
            throw new IllegalArgumentException("Multifactor provider selection via Groovy cannot use blank");
        }
        return providers
                .stream()
                .filter(p -> p.getId().equals(provider))
                .findFirst()
                .get();
    }
}
