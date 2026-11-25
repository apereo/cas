package org.apereo.cas.util.cipher;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.ConfigurationPropertiesBindingContext;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.crypto.CipherExecutorResolver;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link DefaultCipherExecutorResolver}.
 * A tenant gets its own cipher only when its properties set something under the cipher's crypto prefix,
 * such as {@code cas.tgc.crypto}; other tenants use the default cipher. A tenant's cipher is built once
 * per tenant definition and reused, so keys that the tenant leaves out are generated once and not per request.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiredArgsConstructor
public class DefaultCipherExecutorResolver implements CipherExecutorResolver {
    private final CipherExecutor cipherExecutor;
    private final TenantExtractor tenantExtractor;
    private final String cryptoPropertiesPrefix;
    private final Function<ConfigurationPropertiesBindingContext<CasConfigurationProperties>, CipherExecutor> cipherExecutorSupplier;
    private final Cache<TenantDefinition, CipherExecutor> tenantCipherExecutors = Caffeine.newBuilder().weakKeys().build();

    @Override
    public CipherExecutor resolve(final HttpServletRequest request) {
        return tenantExtractor
            .extract(request)
            .map(this::bindTenantToCipherExecutor)
            .orElse(cipherExecutor);
    }

    @Override
    public CipherExecutor resolve(final String tenant) {
        if (StringUtils.isBlank(tenant)) {
            return cipherExecutor;
        }
        val tenantDefinition = tenantExtractor.getTenantsManager().findTenant(tenant).orElseThrow();
        return bindTenantToCipherExecutor(tenantDefinition);
    }

    private CipherExecutor bindTenantToCipherExecutor(final TenantDefinition tenantDefinition) {
        return tenantCipherExecutors.get(tenantDefinition, definition -> {
            val bindingContext = definition.bindProperties();
            return bindingContext.isBound() && bindingContext.containsBindingUnder(cryptoPropertiesPrefix)
                ? cipherExecutorSupplier.apply(bindingContext)
                : cipherExecutor;
        });
    }
}
