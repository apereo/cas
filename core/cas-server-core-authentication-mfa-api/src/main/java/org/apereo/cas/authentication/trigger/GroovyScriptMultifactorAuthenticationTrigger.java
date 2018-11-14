package org.apereo.cas.authentication.trigger;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.ScriptingUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * This is {@link GroovyScriptMultifactorAuthenticationTrigger}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Setter
@RequiredArgsConstructor
@Slf4j
public class GroovyScriptMultifactorAuthenticationTrigger implements MultifactorAuthenticationTrigger {
    private final CasConfigurationProperties casProperties;

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication,
                                                                   final RegisteredService registeredService,
                                                                   final HttpServletRequest httpServletRequest, final Service service) {
        val groovyScript = casProperties.getAuthn().getMfa().getGroovyScript();
        if (groovyScript == null) {
            LOGGER.debug("No groovy script is configured for multifactor authentication");
            return Optional.empty();
        }

        if (!ResourceUtils.doesResourceExist(groovyScript)) {
            LOGGER.warn("No groovy script is found at [{}] for multifactor authentication", groovyScript);
            return Optional.empty();
        }

        if (authentication == null) {
            LOGGER.debug("No authentication is available to determine event for principal");
            return Optional.empty();
        }
        if (registeredService == null) {
            LOGGER.debug("No registered service is available to determine event for principal [{}]", authentication.getPrincipal());
            return Optional.empty();
        }

        val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(ApplicationContextProvider.getApplicationContext());
        if (providerMap.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context");
            throw new AuthenticationException();
        }

        try {
            final Object[] args = {registeredService, registeredService, authentication, httpServletRequest, LOGGER};
            val provider = ScriptingUtils.executeGroovyScript(groovyScript, args, String.class, true);
            LOGGER.debug("Groovy script run for [{}] returned the provider id [{}]", registeredService, provider);
            if (StringUtils.isBlank(provider)) {
                return Optional.empty();
            }
            return MultifactorAuthenticationUtils.resolveProvider(providerMap, provider);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return Optional.empty();
    }
}
