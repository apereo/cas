package org.apereo.cas.authentication.adaptive.intel;

import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.cas.util.ScriptingUtils;

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


    public GroovyIPAddressIntelligenceService(final AdaptiveAuthenticationProperties adaptiveAuthenticationProperties) {
        super(adaptiveAuthenticationProperties);
    }

    @Override
    public IPAddressIntelligenceResponse examineInternal(final RequestContext context, final String clientIpAddress) {
        val groovyResource = adaptiveAuthenticationProperties.getIpIntel().getGroovy().getLocation();
        return ScriptingUtils.executeGroovyScript(groovyResource, new Object[]{context, clientIpAddress, LOGGER}, IPAddressIntelligenceResponse.class, true);
    }
}
