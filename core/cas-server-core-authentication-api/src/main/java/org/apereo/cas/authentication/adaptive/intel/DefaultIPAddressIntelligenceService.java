package org.apereo.cas.authentication.adaptive.intel;

import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;

import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DefaultIPAddressIntelligenceService}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class DefaultIPAddressIntelligenceService extends BaseIPAddressIntelligenceService {
    public DefaultIPAddressIntelligenceService(
        final TenantExtractor tenantExtractor,
        final AdaptiveAuthenticationProperties adaptiveAuthenticationProperties) {
        super(tenantExtractor, adaptiveAuthenticationProperties);
    }

    @Override
    public IPAddressIntelligenceResponse examineInternal(final RequestContext context, final String clientIpAddress) {
        return IPAddressIntelligenceResponse.allowed();
    }
}
