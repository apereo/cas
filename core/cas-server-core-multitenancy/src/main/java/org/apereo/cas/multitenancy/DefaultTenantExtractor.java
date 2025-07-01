package org.apereo.cas.multitenancy;

import org.apereo.cas.configuration.CasConfigurationProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

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

    private final ApplicationContext applicationContext;
    
    private final CasConfigurationProperties casProperties;

    @Override
    public Optional<TenantDefinition> extract(final String requestPath) {
        val tenantId = casProperties.getMultitenancy().getCore().isEnabled()
            ? TenantExtractor.tenantIdFromPath(requestPath)
            : StringUtils.EMPTY;
        return StringUtils.isNotBlank(tenantId) ? tenantsManager.findTenant(tenantId) : Optional.empty();
    }
}
