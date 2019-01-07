package org.apereo.cas.authentication.adaptive.intel;

import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link GroovyIPAddressIntelligenceService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class GroovyIPAddressIntelligenceService extends BaseIPAddressIntelligenceService {
    private final transient WatchableGroovyScriptResource watchableScript;

    public GroovyIPAddressIntelligenceService(final AdaptiveAuthenticationProperties adaptiveAuthenticationProperties) {
        super(adaptiveAuthenticationProperties);
        val groovyScript = adaptiveAuthenticationProperties.getIpIntel().getGroovy().getLocation();
        this.watchableScript = new WatchableGroovyScriptResource(groovyScript);
    }

    @Override
    public IPAddressIntelligenceResponse examineInternal(final RequestContext context, final String clientIpAddress) {
        val args = new Object[]{context, clientIpAddress, LOGGER};
        return watchableScript.execute(args, IPAddressIntelligenceResponse.class);
    }
}
