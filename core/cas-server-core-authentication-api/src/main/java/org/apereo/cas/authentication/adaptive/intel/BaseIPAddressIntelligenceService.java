package org.apereo.cas.authentication.adaptive.intel;

import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.RequestContext;
import java.util.regex.Pattern;

/**
 * This is {@link BaseIPAddressIntelligenceService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseIPAddressIntelligenceService implements IPAddressIntelligenceService {
    protected final TenantExtractor tenantExtractor;
    protected final AdaptiveAuthenticationProperties adaptiveAuthenticationProperties;

    private static void trackResponseInRequestContext(final RequestContext context, final IPAddressIntelligenceResponse response) {
        context.getFlowScope().put("ipAddressIntelligenceResponse", response);
    }

    @Override
    public IPAddressIntelligenceResponse examine(final RequestContext context, final String clientIpAddress) throws Throwable {
        if (isClientIpAddressRejected(clientIpAddress)) {
            val response = IPAddressIntelligenceResponse.banned();
            trackResponseInRequestContext(context, response);
            return response;
        }
        val response = examineInternal(context, clientIpAddress);
        trackResponseInRequestContext(context, response);
        return response;
    }

    protected abstract IPAddressIntelligenceResponse examineInternal(RequestContext context, String clientIpAddress) throws Throwable;

    private boolean isClientIpAddressRejected(final String clientIp) {
        val rejectIpAddresses = adaptiveAuthenticationProperties.getPolicy().getRejectIpAddresses();
        return StringUtils.isNotBlank(rejectIpAddresses)
            && Pattern.compile(rejectIpAddresses).matcher(clientIp).find();
    }
}
