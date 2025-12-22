package org.apereo.cas.web.flow.authentication;

import module java.base;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.Resource;

/**
 * This is {@link GroovyScriptMultifactorAuthenticationProviderSelector}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class GroovyScriptMultifactorAuthenticationProviderSelector implements MultifactorAuthenticationProviderSelector {
    private final ExecutableCompiledScript watchableScript;

    public GroovyScriptMultifactorAuthenticationProviderSelector(final Resource resource) {
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        this.watchableScript = scriptFactory.fromResource(resource);
    }

    @Override
    public @Nullable MultifactorAuthenticationProvider resolve(final Collection<MultifactorAuthenticationProvider> providers,
                                                               @Nullable final RegisteredService service, final Principal principal) throws Throwable {
        val args = new Object[]{Objects.requireNonNull(service), principal, providers, LOGGER};
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
