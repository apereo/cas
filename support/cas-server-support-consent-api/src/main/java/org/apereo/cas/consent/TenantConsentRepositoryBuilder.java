package org.apereo.cas.consent;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.ConfigurationPropertiesBindingContext;
import org.apereo.cas.multitenancy.TenantDefinition;
import lombok.val;

/**
 * This is {@link TenantConsentRepositoryBuilder}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@FunctionalInterface
public interface TenantConsentRepositoryBuilder {
    /**
     * Build consent repository.
     *
     * @param tenantDefinition the tenant definition
     * @return the consent repository
     * @throws Exception the exception
     */
    default List<ConsentRepository> build(final TenantDefinition tenantDefinition) throws Exception{
        if (!tenantDefinition.getProperties().isEmpty()) {
            val bindingContext = tenantDefinition.bindProperties();
            return buildInternal(tenantDefinition, bindingContext);
        }
        return List.of();
    }

    /**
     * No op builder.
     *
     * @return the builder
     */
    static TenantConsentRepositoryBuilder noOp() {
        return (tenantDefinition, bindingContext) -> List.of();
    }
    
    /**
     * Build internal consent repository.
     *
     * @param tenantDefinition the tenant definition
     * @param bindingContext   the cas properties
     * @return the consent repository
     * @throws Exception the exception
     */
    List<ConsentRepository> buildInternal(
        TenantDefinition tenantDefinition,
        ConfigurationPropertiesBindingContext<CasConfigurationProperties> bindingContext) throws Exception;
}
