package org.apereo.cas.syncope;

import org.apereo.cas.authentication.attribute.TenantPersonAttributeDaoBuilder;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.syncope.SyncopePrincipalAttributesProperties;
import org.apereo.cas.configuration.support.ConfigurationPropertiesBindingContext;
import org.apereo.cas.multitenancy.TenantDefinition;
import lombok.val;
import java.util.List;

/**
 * This is {@link TenantSyncopePersonAttributeDaoBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public class TenantSyncopePersonAttributeDaoBuilder implements TenantPersonAttributeDaoBuilder {
    @Override
    public List<? extends PersonAttributeDao> buildInternal(
        final TenantDefinition tenantDefinition,
        final ConfigurationPropertiesBindingContext<CasConfigurationProperties> bindingContext) {
        if (bindingContext.containsBindingFor(SyncopePrincipalAttributesProperties.class)) {
            val casProperties = bindingContext.value();
            val syncope = casProperties.getAuthn().getAttributeRepository().getSyncope();
            return SyncopeUtils.newPersonAttributeDaos(syncope);
        }
        return List.of();
    }
}
