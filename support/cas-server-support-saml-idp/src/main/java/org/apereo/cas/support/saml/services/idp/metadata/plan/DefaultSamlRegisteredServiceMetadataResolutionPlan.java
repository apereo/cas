package org.apereo.cas.support.saml.services.idp.metadata.plan;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link DefaultSamlRegisteredServiceMetadataResolutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class DefaultSamlRegisteredServiceMetadataResolutionPlan implements SamlRegisteredServiceMetadataResolutionPlan {
    private List<SamlRegisteredServiceMetadataResolver> resolvers = new ArrayList<>();
    
    @Override
    public void registerMetadataResolver(final SamlRegisteredServiceMetadataResolver clazz) {
        resolvers.add(clazz);
    }

    @Override
    public Collection<SamlRegisteredServiceMetadataResolver> getRegisteredMetadataResolvers() {
        return this.resolvers;
    }
}
