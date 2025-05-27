package org.apereo.cas.authentication.adaptive.intel;

import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
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
    private final ExecutableCompiledScript watchableScript;

    public GroovyIPAddressIntelligenceService(
        final TenantExtractor tenantExtractor,
        final AdaptiveAuthenticationProperties adaptiveAuthenticationProperties) {
        super(tenantExtractor, adaptiveAuthenticationProperties);
        val groovyScript = adaptiveAuthenticationProperties.getIpIntel().getGroovy().getLocation();
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        this.watchableScript = scriptFactory.fromResource(groovyScript);
    }

    @Override
    public IPAddressIntelligenceResponse examineInternal(final RequestContext context, final String clientIpAddress) throws Throwable {
        val args = new Object[]{context, clientIpAddress, LOGGER};
        return watchableScript.execute(args, IPAddressIntelligenceResponse.class);
    }
}
