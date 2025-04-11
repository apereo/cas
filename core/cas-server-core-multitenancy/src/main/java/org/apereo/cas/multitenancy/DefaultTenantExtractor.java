package org.apereo.cas.multitenancy;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).minimal(true).sorted(true).build().toObjectMapper();

    private final TenantsManager tenantsManager;

    private final CasConfigurationProperties casProperties;

    @Override
    public Optional<TenantDefinition> extract(final String requestPath) {
        val tenantId = casProperties.getMultitenancy().getCore().isEnabled()
            ? TenantExtractor.tenantIdFromPath(requestPath)
            : StringUtils.EMPTY;
        return StringUtils.isNotBlank(tenantId) ? tenantsManager.findTenant(tenantId) : Optional.empty();
    }

    @Override
    public String getTenantKey(final TenantDefinition tenantDefinition) {
        return FunctionUtils.doUnchecked(() -> {
            val record = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(tenantDefinition);
            return DigestUtils.sha512(record);
        });
    }


}
