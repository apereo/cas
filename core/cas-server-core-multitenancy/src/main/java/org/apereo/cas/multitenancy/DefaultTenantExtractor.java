package org.apereo.cas.multitenancy;

import org.apereo.cas.util.RegexUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * This is {@link DefaultTenantExtractor}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultTenantExtractor implements TenantExtractor {
    private static final Pattern PATTERN_TENANTS = RegexUtils.createPattern("tenants/(.+)/(.+)");
    private final TenantsManager tenantsManager;

    @Override
    public Optional<TenantDefinition> extract(final String requestPath) {
        val matcher = PATTERN_TENANTS.matcher(requestPath);
        if (matcher.find()) {
            val tenantId = matcher.group(1).trim();
            if (StringUtils.isNotBlank(tenantId)) {
                return tenantsManager.findTenant(tenantId);
            }
        }
        return Optional.empty();
    }
}
