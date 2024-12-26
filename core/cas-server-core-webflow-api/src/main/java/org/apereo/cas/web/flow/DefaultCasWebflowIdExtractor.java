package org.apereo.cas.web.flow;

import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.multitenancy.TenantsManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public String extract(final HttpServletRequest request, final String flowId) {
        return tenantExtractor.extract(flowId)
            .map(__ -> flowId.substring(flowId.lastIndexOf('/') + 1))
            .orElse(flowId);
    }
}
