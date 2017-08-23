package org.apereo.cas.support.saml.services.idp.metadata.cache;

import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.opensaml.saml.metadata.resolver.MetadataResolver;

/**
 * This is {@link SamlRegisteredServiceCachingMetadataResolver}
 * that defines how metadata is to be resolved and cached for a given saml
 * registered service.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@FunctionalInterface
public interface SamlRegisteredServiceCachingMetadataResolver {

    /**
     * Resolve chaining metadata resolver.
     *
     * @param service the service
     * @return the chaining metadata resolver
     */
    MetadataResolver resolve(SamlRegisteredService service);
}
