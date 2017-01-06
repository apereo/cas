package org.apereo.cas.support.saml.mdui;

import org.opensaml.saml.saml2.metadata.EntityDescriptor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is {@link ChainingMetadataResolverAdapter}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class ChainingMetadataResolverAdapter implements MetadataResolverAdapter {

    private final Set<MetadataResolverAdapter> adapters;

    public ChainingMetadataResolverAdapter(final List<MetadataResolverAdapter> adapters) {
        this.adapters = new HashSet<>(adapters);
    }

    @Override
    public EntityDescriptor getEntityDescriptorForEntityId(final String entityId) {
        for (final MetadataResolverAdapter adapter : adapters) {
            final EntityDescriptor e = adapter.getEntityDescriptorForEntityId(entityId);
            if (e != null) {
                return e;
            }
        }
        return null;
    }
}
