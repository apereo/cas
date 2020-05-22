package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderAbsentException;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledGroovyScript;
import org.apereo.cas.util.scripting.GroovyShellScript;
import org.apereo.cas.util.scripting.ScriptingUtils;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;

import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is {@link ScriptedRegisteredServiceMultifactorAuthenticationTrigger}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 * @deprecated Since 6.2
 */
@Getter
@Setter
@Slf4j
@RequiredArgsConstructor
@Deprecated(since = "6.2.0")
public class ScriptedRegisteredServiceMultifactorAuthenticationTrigger implements MultifactorAuthenticationTrigger {
    private final CasConfigurationProperties casProperties;

    private final ApplicationContext applicationContext;

    private int order = Ordered.LOWEST_PRECEDENCE;

    @JsonIgnore
    @Transient
    @org.springframework.data.annotation.Transient
    private Map<String, ExecutableCompiledGroovyScript> scriptCache = new ConcurrentHashMap<>(0);

    @Override
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication,
                                                                   final RegisteredService registeredService,
                                                                   final HttpServletRequest httpServletRequest,
                                                                   final Service service) {

        if (this.scriptCache == null) {
            this.scriptCache = new LinkedHashMap<>(1);
        }

        if (authentication == null || registeredService == null) {
            LOGGER.debug("No authentication or service is available to determine event for principal");
            return Optional.empty();
        }

        val policy = registeredService.getMultifactorPolicy();
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

        LOGGER.trace("Locating multifactor authentication trigger script [{}] in script cache...", mfaScript);

        if (!scriptCache.containsKey(mfaScript)) {
            val matcherInline = ScriptingUtils.getMatcherForInlineGroovyScript(mfaScript);
            val matcherFile = ScriptingUtils.getMatcherForExternalGroovyScript(mfaScript);

            if (matcherInline.find()) {
                val script = new GroovyShellScript(matcherInline.group(1));
                scriptCache.put(mfaScript, script);
                LOGGER.trace("Caching multifactor authentication trigger script as an executable shell script");
            } else if (matcherFile.find()) {
                try {
                    val scriptPath = SpringExpressionLanguageValueResolver.getInstance().resolve(matcherFile.group());
                    val resource = ResourceUtils.getRawResourceFrom(scriptPath);
                    val script = new WatchableGroovyScriptResource(resource);
                    scriptCache.put(mfaScript, script);
                    LOGGER.trace("Caching multifactor authentication trigger script as script resource [{}]", resource);
                } catch (final Exception e) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.error(e.getMessage(), e);
                    } else {
                        LOGGER.error(e.getMessage());
                    }
                }
            }
        }

        val executableScript = scriptCache.get(mfaScript);
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
}

