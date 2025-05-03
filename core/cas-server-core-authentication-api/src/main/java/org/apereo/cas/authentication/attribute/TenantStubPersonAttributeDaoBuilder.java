package org.apereo.cas.authentication.attribute;

import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.configuration.model.core.authentication.StubPrincipalAttributesProperties;
import org.apereo.cas.multitenancy.TenantDefinition;
import lombok.val;
import java.util.List;

/**
 * This is {@link TenantStubPersonAttributeDaoBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public class TenantStubPersonAttributeDaoBuilder implements TenantPersonAttributeDaoBuilder {
    @Override
    public List<? extends PersonAttributeDao> build(final TenantDefinition tenantDefinition) {
        val bindingContext = tenantDefinition.bindProperties();
        if (bindingContext.isBound() && bindingContext.containsBindingFor(StubPrincipalAttributesProperties.class)) {
            val casProperties = bindingContext.value();
            val stub = casProperties.getAuthn().getAttributeRepository().getStub();
            val repository = PersonAttributeUtils.newStubAttributeRepository(stub);
            return List.of(repository.markDisposable());
        }
        return List.of();
    }
}

