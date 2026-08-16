package org.apereo.cas.aup;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.ConfigurationPropertiesBindingContext;
import org.apereo.cas.multitenancy.TenantDefinition;
import lombok.val;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link TenantAcceptableUsagePolicyRepositoryBuilder}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@FunctionalInterface
public interface TenantAcceptableUsagePolicyRepositoryBuilder {
    /**
     * Build acceptable usage policy repository.
     *
     * @param tenantDefinition the tenant definition
     * @return the acceptable usage policy repository
     * @throws Exception the exception
     */
    default @Nullable AcceptableUsagePolicyRepository build(final TenantDefinition tenantDefinition) throws Exception {
        if (!tenantDefinition.getProperties().isEmpty()) {
            val bindingContext = tenantDefinition.bindProperties();
            return buildInternal(tenantDefinition, bindingContext);
        }
        return null;
    }

    /**
     * Build internal acceptable usage policy repository.
     *
     * @param tenantDefinition the tenant definition
     * @param bindingContext   the binding context
     * @return the acceptable usage policy repository
     */
    @Nullable AcceptableUsagePolicyRepository buildInternal(TenantDefinition tenantDefinition,
                                                            ConfigurationPropertiesBindingContext<CasConfigurationProperties> bindingContext);
}
