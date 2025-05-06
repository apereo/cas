package org.apereo.cas.persondir;

import org.apereo.cas.authentication.attribute.TenantPersonAttributeDaoBuilder;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.RestPrincipalAttributesProperties;
import org.apereo.cas.configuration.support.ConfigurationPropertiesBindingContext;
import org.apereo.cas.multitenancy.TenantDefinition;
import lombok.val;
import java.util.List;

/**
 * This is {@link TenantRestfulPersonAttributeDaoBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public class TenantRestfulPersonAttributeDaoBuilder implements TenantPersonAttributeDaoBuilder {
    @Override
    public List<? extends PersonAttributeDao> buildInternal(final TenantDefinition tenantDefinition,
                                                            final ConfigurationPropertiesBindingContext<CasConfigurationProperties> bindingContext) {
        if (bindingContext.containsBindingFor(RestPrincipalAttributesProperties.class)) {
            val casProperties = bindingContext.value();
            val rest = casProperties.getAuthn().getAttributeRepository().getRest();
            return PersonDirectoryUtils.newRestAttributeRepository(rest);
        }
        return List.of();
    }
}
