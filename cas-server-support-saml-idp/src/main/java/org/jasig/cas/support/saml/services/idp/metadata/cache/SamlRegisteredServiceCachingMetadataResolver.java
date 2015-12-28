package org.jasig.cas.support.saml.services.idp.metadata.cache;

import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;

/**
 * This is {@link SamlRegisteredServiceCachingMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public interface SamlRegisteredServiceCachingMetadataResolver {

    /**
     * Resolve chaining metadata resolver.
     *
     * @param service the service
     * @return the chaining metadata resolver
     */
    ChainingMetadataResolver resolve(SamlRegisteredService service);
}
