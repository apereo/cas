package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.ChainingMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * This is {@link GroovyScriptMultifactorAuthenticationTrigger}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Setter
@Slf4j
@RequiredArgsConstructor
public class GroovyScriptMultifactorAuthenticationTrigger implements MultifactorAuthenticationTrigger, DisposableBean {
    private final ExecutableCompiledScript watchableScript;

    private final ApplicationContext applicationContext;

    private final MultifactorAuthenticationProviderResolver multifactorAuthenticationProviderResolver;

    private final MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector;

    private final TenantExtractor tenantExtractor;
    
    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication,
                                                                   final RegisteredService registeredService,
                                                                   final HttpServletRequest httpServletRequest,
                                                                   final HttpServletResponse response,
                                                                   final Service service) {
        if (authentication == null) {
            LOGGER.debug("No authentication is available to determine event for principal");
            return Optional.empty();
        }

        val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context");
            throw new AuthenticationException();
        }

        return FunctionUtils.doUnchecked(() -> {
            val args = new Object[]{service, registeredService, authentication, httpServletRequest, LOGGER};
            val provider = this.watchableScript.execute(args, String.class);
            LOGGER.debug("Groovy script run for [{}] returned the provider id [{}]", registeredService, provider);
            if (StringUtils.isBlank(provider)) {
                return Optional.empty();
            }
            val principal = multifactorAuthenticationProviderResolver.resolvePrincipal(authentication.getPrincipal());
            if (provider.equals(ChainingMultifactorAuthenticationProvider.DEFAULT_IDENTIFIER)) {
                return Optional.of(multifactorAuthenticationProviderSelector.resolve(providerMap.values(), registeredService, principal));
            }
            return MultifactorAuthenticationUtils.resolveProvider(providerMap, provider);
        });

    }

    @Override
    public void destroy() {
        this.watchableScript.close();
    }
}
