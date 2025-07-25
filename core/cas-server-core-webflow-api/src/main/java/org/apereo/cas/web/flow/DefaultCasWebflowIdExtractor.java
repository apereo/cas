package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.multitenancy.TenantsManager;
import org.apereo.cas.multitenancy.UnknownTenantException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link DefaultCasWebflowIdExtractor}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Getter
@Slf4j
@RequiredArgsConstructor
public class DefaultCasWebflowIdExtractor implements CasWebflowIdExtractor {
    private final TenantExtractor tenantExtractor;
    private final TenantsManager tenantsManager;
    private final CasConfigurationProperties casProperties;

    @Override
    public String extract(final HttpServletRequest request, final String flowId) {
        if (casProperties.getMultitenancy().getCore().isEnabled() && isValidFlowId(flowId)) {
            val tenant = tenantExtractor.extract(request).orElseThrow(
                () -> new UnknownTenantException("Unknown tenant definition for flow id " + flowId));
            request.setAttribute(TenantDefinition.class.getName(), tenant);
            return flowId.substring(flowId.lastIndexOf('/') + 1);
        }
        return flowId;
    }

    private static boolean isValidFlowId(final String flowId) {
        return !Strings.CI.startsWith(flowId, "webjars/")
            && !Strings.CI.startsWith(flowId, "css/")
            && !Strings.CI.startsWith(flowId, "favicon")
            && !Strings.CI.startsWith(flowId, "images/")
            && !Strings.CI.startsWith(flowId, "js/");
    }
}
