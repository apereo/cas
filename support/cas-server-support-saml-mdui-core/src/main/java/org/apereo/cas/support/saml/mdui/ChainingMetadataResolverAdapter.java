package org.apereo.cas.support.saml.mdui;

import lombok.RequiredArgsConstructor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

import java.util.Objects;
import java.util.Set;

/**
 * This is {@link ChainingMetadataResolverAdapter}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
public class ChainingMetadataResolverAdapter implements MetadataResolverAdapter {

    private final Set<MetadataResolverAdapter> adapters;

    @Override
    public EntityDescriptor getEntityDescriptorForEntityId(final String entityId) {
        return adapters.stream().map(adapter -> adapter.getEntityDescriptorForEntityId(entityId)).filter(Objects::nonNull).findFirst().orElse(null);
    }
}
