package org.apereo.cas.support.saml;

import net.shibboleth.shared.resolver.CriteriaSet;
import org.opensaml.saml.metadata.resolver.impl.AbstractMetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

/**
 * This is {@link EntityDescriptorMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public class EntityDescriptorMetadataResolver extends AbstractMetadataResolver {
    private final EntityDescriptor entityDescriptor;

    public EntityDescriptorMetadataResolver(final EntityDescriptor entityDescriptor, final OpenSamlConfigBean openSamlConfigBean) {
        this.entityDescriptor = entityDescriptor;
        setId(Objects.requireNonNull(entityDescriptor.getEntityID()));
        setRequireValidMetadata(true);
        setParserPool(openSamlConfigBean.getParserPool());
        setFailFastInitialization(true);
    }

    @Nonnull
    @Override
    protected Iterable<EntityDescriptor> doResolve(final CriteriaSet criteria) {
        return List.of(entityDescriptor);
    }
}
           
