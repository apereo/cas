package org.apereo.cas.util.cipher;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.ConfigurationPropertiesBindingContext;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.crypto.CipherExecutorResolver;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.function.Function;

/**
 * This is {@link DefaultCipherExecutorResolver}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiredArgsConstructor
public class DefaultCipherExecutorResolver implements CipherExecutorResolver {
    private final CipherExecutor cipherExecutor;
    private final TenantExtractor tenantExtractor;
    private final Class cryptoPropertiesClass;
    private final Function<ConfigurationPropertiesBindingContext<CasConfigurationProperties>, CipherExecutor> cipherExecutorSupplier;

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
        val bindingContext = tenantDefinition.bindProperties();
        if (bindingContext.isBound() && bindingContext.containsBindingFor(cryptoPropertiesClass)) {
            return cipherExecutorSupplier.apply(bindingContext);
        }
        return cipherExecutor;
    }
}
