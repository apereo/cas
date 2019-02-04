package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.scripting.ScriptingUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;

import java.util.Collection;

/**
 * This is {@link GroovyScriptMultifactorAuthenticationProviderSelector}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class GroovyScriptMultifactorAuthenticationProviderSelector implements MultifactorAuthenticationProviderSelector {
    private final Resource groovyScript;

    @Override
    public MultifactorAuthenticationProvider resolve(final Collection<MultifactorAuthenticationProvider> providers,
                                                     final RegisteredService service, final Principal principal) {
        val args = new Object[]{service, principal, providers, LOGGER};
        val provider = ScriptingUtils.executeGroovyScript(groovyScript, args, String.class, true);
        LOGGER.debug("Invoking Groovy script with service=[{}], principal=[{}], providers=[{}] and default logger", service, principal, providers);
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
