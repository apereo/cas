package org.apereo.cas.persondir;

import org.apereo.cas.authentication.attribute.TenantPersonAttributeDaoBuilder;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapPrincipalAttributesProperties;
import org.apereo.cas.configuration.support.ConfigurationPropertiesBindingContext;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.util.LdapUtils;
import lombok.val;
import java.util.List;

/**
 * This is {@link TenantLdapPersonAttributeDaoBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public class TenantLdapPersonAttributeDaoBuilder implements TenantPersonAttributeDaoBuilder {
    @Override
    public List<? extends PersonAttributeDao> buildInternal(
        final TenantDefinition tenantDefinition,
        final ConfigurationPropertiesBindingContext<CasConfigurationProperties> bindingContext) {
        if (bindingContext.containsBindingFor(LdapPrincipalAttributesProperties.class)) {
            val casProperties = bindingContext.value();
            val ldapProperties = casProperties.getAuthn().getAttributeRepository().getLdap();
            return LdapUtils.newPersonAttributeDaos(ldapProperties);
        }
        return List.of();
    }
}

