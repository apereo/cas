package org.apereo.cas.authentication;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.ScriptingUtils;
import org.springframework.core.io.Resource;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link GroovyMultifactorAuthenticationProviderBypass}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class GroovyMultifactorAuthenticationProviderBypass extends AbstractMultifactorAuthenticationProviderBypass {
    private static final long serialVersionUID = -4909072898415688377L;

    private final transient Resource groovyScript;

    public GroovyMultifactorAuthenticationProviderBypass(final MultifactorAuthenticationProviderBypassProperties bypass) {
        super(bypass);
        this.groovyScript = bypass.getGroovy().getLocation();
    }

    @Override
    public boolean shouldMultifactorAuthenticationProviderExecute(final Authentication authentication,
                                                                  final RegisteredService registeredService,
                                                                  final MultifactorAuthenticationProvider provider,
                                                                  final HttpServletRequest request) {
        try {
            final Principal principal = authentication.getPrincipal();
            LOGGER.debug("Evaluating multifactor authentication bypass properties for principal [{}], "
                    + "service [{}] and provider [{}] via Groovy script [{}]",
                principal.getId(), registeredService, provider, this.groovyScript);
            final boolean shouldExecute = ScriptingUtils.executeGroovyScript(this.groovyScript,
                new Object[]{authentication, principal, registeredService, provider, LOGGER, request}, Boolean.class);
            if (shouldExecute) {
                updateAuthenticationToForgetBypass(authentication, provider, principal);
            } else {
                LOGGER.info("Groovy bypass script determined [{}] would be passed for [{}]", principal.getId(), provider.getId());
                updateAuthenticationToRememberBypass(authentication, provider, principal);
            }
            return shouldExecute;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.error("Error encountered executing groovy bypass, returning shouldExecute true as default");
        return true;
    }
}
