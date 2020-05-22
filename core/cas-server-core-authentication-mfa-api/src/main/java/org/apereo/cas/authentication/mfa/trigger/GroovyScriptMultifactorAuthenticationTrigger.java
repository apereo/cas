package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
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
@Slf4j
public class GroovyScriptMultifactorAuthenticationTrigger implements MultifactorAuthenticationTrigger, DisposableBean {
    private final CasConfigurationProperties casProperties;
    private final WatchableGroovyScriptResource watchableScript;
    private final ApplicationContext applicationContext;

    private int order = Ordered.LOWEST_PRECEDENCE;

    public GroovyScriptMultifactorAuthenticationTrigger(final CasConfigurationProperties casProperties,
                                                        final ApplicationContext applicationContext) {
        this.casProperties = casProperties;
        val groovyScript = casProperties.getAuthn().getMfa().getGroovyScript();
        this.watchableScript = new WatchableGroovyScriptResource(groovyScript);
        this.applicationContext = applicationContext;
    }

    @Override
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication,
                                                                   final RegisteredService registeredService,
                                                                   final HttpServletRequest httpServletRequest, final Service service) {
        val groovyScript = casProperties.getAuthn().getMfa().getGroovyScript();
        if (groovyScript == null) {
            LOGGER.trace("No groovy script is configured for multifactor authentication");
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

        if (service == null) {
            LOGGER.debug("No service is available to determine event for principal [{}]", authentication.getPrincipal());
            return Optional.empty();
        }
        
        val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context");
            throw new AuthenticationException();
        }

        try {
            val args = new Object[]{service, registeredService, authentication, httpServletRequest, LOGGER};
            val provider = this.watchableScript.execute(args, String.class);
            LOGGER.debug("Groovy script run for [{}] returned the provider id [{}]", registeredService, provider);
            if (StringUtils.isBlank(provider)) {
                return Optional.empty();
            }
            return MultifactorAuthenticationUtils.resolveProvider(providerMap, provider);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return Optional.empty();
    }

    @Override
    public void destroy() {
        this.watchableScript.close();
    }
}
