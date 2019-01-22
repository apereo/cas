package org.apereo.cas.authentication.bypass;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link GroovyMultifactorAuthenticationProviderBypass}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class GroovyMultifactorAuthenticationProviderBypass extends BaseMultifactorAuthenticationProviderBypass {
    private static final long serialVersionUID = -4909072898415688377L;

    private final transient WatchableGroovyScriptResource watchableScript;

    public GroovyMultifactorAuthenticationProviderBypass(final MultifactorAuthenticationProviderBypassProperties bypassProperties,
                                                         final String providerId) {
        super(providerId);
        val groovyScript = bypassProperties.getGroovy().getLocation();
        this.watchableScript = new WatchableGroovyScriptResource(groovyScript);
    }

    @Override
    public boolean shouldMultifactorAuthenticationProviderExecute(final Authentication authentication,
                                                                  final RegisteredService registeredService,
                                                                  final MultifactorAuthenticationProvider provider,
                                                                  final HttpServletRequest request) {
        try {
            val principal = authentication.getPrincipal();
            LOGGER.debug("Evaluating multifactor authentication bypass properties for principal [{}], "
                    + "service [{}] and provider [{}] via Groovy script [{}]",
                principal.getId(), registeredService, provider, watchableScript.getResource());
            val args = new Object[]{authentication, principal, registeredService, provider, LOGGER, request};
            return watchableScript.execute(args, Boolean.class);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return true;
        }
    }
}
