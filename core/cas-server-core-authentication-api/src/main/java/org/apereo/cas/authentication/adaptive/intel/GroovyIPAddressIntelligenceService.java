package org.apereo.cas.authentication.adaptive.intel;

import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.cas.util.ScriptingUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link GroovyIPAddressIntelligenceService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class GroovyIPAddressIntelligenceService extends BaseIPAddressIntelligenceService {
    private final transient Resource groovyResource;

    public GroovyIPAddressIntelligenceService(final AdaptiveAuthenticationProperties adaptiveAuthenticationProperties, final Resource groovyResource) {
        super(adaptiveAuthenticationProperties);
        this.groovyResource = groovyResource;
    }

    @Override
    public IPAddressIntelligenceResponse examineInternal(final RequestContext context, final String clientIpAddress) {
        return ScriptingUtils.executeGroovyScript(this.groovyResource, new Object[]{context, clientIpAddress, LOGGER}, IPAddressIntelligenceResponse.class);
    }
}
