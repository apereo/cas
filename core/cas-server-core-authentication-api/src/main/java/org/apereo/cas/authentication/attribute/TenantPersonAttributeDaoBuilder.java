package org.apereo.cas.authentication.attribute;

import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.multitenancy.TenantDefinition;
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
    List<? extends PersonAttributeDao> build(TenantDefinition tenantDefinition);
}
