package org.apereo.cas.jdbc;

import org.apereo.cas.authentication.attribute.TenantPersonAttributeDaoBuilder;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jdbc.JdbcPrincipalAttributesProperties;
import org.apereo.cas.configuration.support.ConfigurationPropertiesBindingContext;
import org.apereo.cas.multitenancy.TenantDefinition;
import lombok.val;
import java.util.List;

/**
 * This is {@link TenantJdbcPersonAttributeDaoBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public class TenantJdbcPersonAttributeDaoBuilder implements TenantPersonAttributeDaoBuilder {
    @Override
    public List<? extends PersonAttributeDao> buildInternal(final TenantDefinition tenantDefinition,
                                                            final ConfigurationPropertiesBindingContext<CasConfigurationProperties> bindingContext) {
        if (bindingContext.containsBindingFor(JdbcPrincipalAttributesProperties.class)) {
            val casProperties = bindingContext.value();
            val jdbc = casProperties.getAuthn().getAttributeRepository().getJdbc();
            return JdbcPersonAttributeUtils.newJdbcAttributeRepositoryDao(jdbc);
        }
        return List.of();
    }
}
