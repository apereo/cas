package org.apereo.cas.authentication.bypass;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ConfigurableApplicationContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link GroovyMultifactorAuthenticationProviderBypassEvaluator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class GroovyMultifactorAuthenticationProviderBypassEvaluator extends BaseMultifactorAuthenticationProviderBypassEvaluator {
    @Serial
    private static final long serialVersionUID = -4909072898415688377L;

    @Nullable
    private final ExecutableCompiledScript watchableScript;

    public GroovyMultifactorAuthenticationProviderBypassEvaluator(final MultifactorAuthenticationProviderBypassProperties bypassProperties,
                                                                  final String providerId,
                                                                  final ConfigurableApplicationContext applicationContext) {
        super(providerId, applicationContext);
        val groovyScript = bypassProperties.getGroovy().getLocation();
        val scriptFactory = ExecutableCompiledScriptFactory.findExecutableCompiledScriptFactory();
        this.watchableScript = scriptFactory.map(factory -> factory.fromResource(groovyScript)).orElse(null);
    }

    @Override
    public boolean shouldMultifactorAuthenticationProviderExecuteInternal(final Authentication authentication,
                                                                          @Nullable final RegisteredService registeredService,
                                                                          final MultifactorAuthenticationProvider provider,
                                                                          @Nullable final HttpServletRequest request) {
        return watchableScript != null && FunctionUtils.doAndHandle(() -> {
            val principal = resolvePrincipal(authentication.getPrincipal());
            LOGGER.debug("Evaluating multifactor authentication bypass properties for principal [{}], "
                         + "service [{}] and provider [{}] via Groovy script [{}]",
                principal.getId(), registeredService, provider, watchableScript);
            val args = new Object[]{authentication, principal, registeredService, provider, LOGGER, request};
            return Boolean.TRUE.equals(watchableScript.execute(args, Boolean.class));
        }, e -> true).get();

    }
}
