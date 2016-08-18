package org.apereo.cas.support.saml.web.flow.mdui;

import org.opensaml.saml.saml2.metadata.EntityDescriptor;

import java.util.HashSet;
import java.util.Set;

/**
 * This is {@link ChainingMetadataResolverAdapter}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class ChainingMetadataResolverAdapter implements MetadataResolverAdapter {
    private Set<MetadataResolverAdapter> adapters = new HashSet<>();

    public Set<MetadataResolverAdapter> getAdapters() {
        return adapters;
    }

    public void setAdapters(final Set<MetadataResolverAdapter> adapters) {
        this.adapters = adapters;
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
