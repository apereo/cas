package org.apereo.cas.support.saml.services.idp.metadata.plan;

import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;

import java.util.Collection;

/**
 * This is {@link SamlRegisteredServiceMetadataResolutionPlan} that allows modules to register
 * metadata resolvers dynamically.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public interface SamlRegisteredServiceMetadataResolutionPlan {

    /**
     * Register resolver.
     *
     * @param clazz the clazz to register
     */
    void registerMetadataResolver(SamlRegisteredServiceMetadataResolver clazz);


    /**
     * Gets registered resolvers.
     *
     * @return the registered classes
     */
    Collection<SamlRegisteredServiceMetadataResolver> getRegisteredMetadataResolvers();
}
