package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderAbsentException;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * This is {@link ScriptedRegisteredServiceMultifactorAuthenticationTrigger}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Getter
@Slf4j
@RequiredArgsConstructor
public class ScriptedRegisteredServiceMultifactorAuthenticationTrigger implements MultifactorAuthenticationTrigger {
    private final CasConfigurationProperties casProperties;

    private final ApplicationContext applicationContext;

    private final TenantExtractor tenantExtractor;
    
    @Setter
    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication,
                                                                   final RegisteredService registeredService,
                                                                   final HttpServletRequest httpServletRequest,
                                                                   final HttpServletResponse response,
                                                                   final Service service) throws Throwable {
        if (authentication == null || registeredService == null) {
            LOGGER.debug("No authentication or service is available to determine event for principal");
            return Optional.empty();
        }

        val policy = registeredService.getMultifactorAuthenticationPolicy();
        if (policy == null || StringUtils.isBlank(policy.getScript())) {
            LOGGER.trace("Multifactor authentication policy is absent or does not define a script to trigger multifactor authentication");
            return Optional.empty();
        }

        val mfaScript = policy.getScript();
        val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context");
            throw new AuthenticationException(new MultifactorAuthenticationProviderAbsentException());
        }

        LOGGER.trace("Locating multifactor authentication trigger script [{}]...", mfaScript);
        val executableScript = fetchScript(mfaScript);
        if (executableScript != null) {
            LOGGER.debug("Executing multifactor authentication trigger script [{}]", executableScript);
            val result = executableScript.execute(new Object[]{authentication, registeredService, httpServletRequest,
                service, applicationContext, LOGGER}, String.class);
            LOGGER.debug("Multifactor authentication provider delivered by trigger script is [{}]", result);
            if (StringUtils.isBlank(result)) {
                LOGGER.debug("No multifactor authentication is returned from trigger script");
                return Optional.empty();
            }
            val providerResult = providerMap.values().stream().filter(provider -> provider.getId().equalsIgnoreCase(result)).findFirst();
            if (providerResult.isEmpty()) {
                LOGGER.error("Unable to locate multifactor authentication provider [{}] in the application context", result);
                throw new AuthenticationException(new MultifactorAuthenticationProviderAbsentException());
            }
            return providerResult;
        }
        return Optional.empty();
    }

    protected ExecutableCompiledScript fetchScript(final String mfaScript) throws Exception {
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        if (scriptFactory.isInlineScript(mfaScript) && CasRuntimeHintsRegistrar.notInNativeImage()) {
            return scriptFactory.fromScript(scriptFactory.getInlineScript(mfaScript).orElseThrow());
        }
        if (scriptFactory.isExternalScript(mfaScript)) {
            val scriptPath = SpringExpressionLanguageValueResolver.getInstance()
                .resolve(scriptFactory.getExternalScript(mfaScript).orElseThrow());
            val resource = ResourceUtils.getResourceFrom(scriptPath);
            return scriptFactory.fromResource(resource);
        }
        return null;
    }
}

