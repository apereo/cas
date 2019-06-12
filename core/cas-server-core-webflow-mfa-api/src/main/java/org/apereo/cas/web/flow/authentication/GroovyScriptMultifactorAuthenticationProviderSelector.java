package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

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
public class GroovyScriptMultifactorAuthenticationProviderSelector implements MultifactorAuthenticationProviderSelector {
    private final transient WatchableGroovyScriptResource watchableScript;

    public GroovyScriptMultifactorAuthenticationProviderSelector(final Resource resource) {
        this.watchableScript = new WatchableGroovyScriptResource(resource);
    }

    @Override
    public MultifactorAuthenticationProvider resolve(final Collection<MultifactorAuthenticationProvider> providers,
                                                     final RegisteredService service, final Principal principal) {
        val args = new Object[]{service, principal, providers, LOGGER};
        LOGGER.debug("Invoking Groovy script with service=[{}], principal=[{}], providers=[{}]", service, principal, providers);
        val provider = watchableScript.execute(args, String.class);
        if (StringUtils.isBlank(provider)) {
            LOGGER.debug("Multifactor provider selection script did not return a provider id");
            return null;
        }
        return providers
            .stream()
            .filter(p -> p.getId().equals(provider))
            .findFirst()
            .orElse(null);
    }
}
