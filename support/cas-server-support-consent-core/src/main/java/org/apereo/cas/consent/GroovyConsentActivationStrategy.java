package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.Resource;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link GroovyConsentActivationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class GroovyConsentActivationStrategy implements ConsentActivationStrategy, DisposableBean {
    private final ConsentEngine consentEngine;

    private final CasConfigurationProperties casProperties;

    private final transient WatchableGroovyScriptResource watchableScript;

    public GroovyConsentActivationStrategy(final Resource groovyResource,
                                           final ConsentEngine consentEngine,
                                           final CasConfigurationProperties casProperties) {
        this.watchableScript = new WatchableGroovyScriptResource(groovyResource);
        this.consentEngine = consentEngine;
        this.casProperties = casProperties;
    }

    @Override
    public boolean isConsentRequired(final Service service, final RegisteredService registeredService,
                                     final Authentication authentication,
                                     final HttpServletRequest requestContext) {
        val args = new Object[]{consentEngine, casProperties, service,
            registeredService, authentication, requestContext, LOGGER};
        return watchableScript.execute(args, Boolean.class);
    }

    @Override
    public void destroy() {
        this.watchableScript.close();
    }
}
