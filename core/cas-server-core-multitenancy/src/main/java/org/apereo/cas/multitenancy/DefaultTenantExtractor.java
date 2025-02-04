package org.apereo.cas.multitenancy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import java.util.Optional;

/**
 * This is {@link DefaultTenantExtractor}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiredArgsConstructor
@Slf4j
@Getter
public class DefaultTenantExtractor implements TenantExtractor {

    private final TenantsManager tenantsManager;

    @Override
    public Optional<TenantDefinition> extract(final String requestPath) {
        val tenantId = TenantExtractor.tenantIdFromPath(requestPath);
        return StringUtils.isNotBlank(tenantId) ? tenantsManager.findTenant(tenantId) : Optional.empty();
    }
}
