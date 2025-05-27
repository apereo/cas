package org.apereo.cas.authentication.attribute;

import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.ConfigurationPropertiesBindingContext;
import org.apereo.cas.multitenancy.TenantDefinition;
import lombok.val;
import java.util.List;

/**
 * This is {@link TenantPersonAttributeDaoBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@FunctionalInterface
public interface TenantPersonAttributeDaoBuilder {
    /**
     * Build list.
     *
     * @param tenantDefinition the tenant definition
     * @return the list
     */
    default List<? extends PersonAttributeDao> build(final TenantDefinition tenantDefinition) {
        val bindingContext = tenantDefinition.bindProperties();
        if (bindingContext.isBound()) {
            val repositories = buildInternal(tenantDefinition, bindingContext);
            repositories.forEach(PersonAttributeDao::markDisposable);
            return repositories;
        }
        return List.of();
    }

    /**
     * Build internal repositories.
     *
     * @param tenantDefinition the tenant definition
     * @param bindingContext   the binding context
     * @return the list
     */
    List<? extends PersonAttributeDao> buildInternal(TenantDefinition tenantDefinition,
                                                     ConfigurationPropertiesBindingContext<CasConfigurationProperties> bindingContext);


    /**
     * No op tenant person attribute dao builder.
     *
     * @return the tenant person attribute dao builder
     */
    static TenantPersonAttributeDaoBuilder noOp() {
        return (tenantDefinition, bindingContext) -> List.of();
    }
}
