package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.opensaml.saml.metadata.resolver.MetadataResolver;

import java.util.Collection;

/**
 * This is {@link SamlRegisteredServiceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public interface SamlRegisteredServiceMetadataResolver {

    /**
     * Resolve list.
     *
     * @param service the service
     * @return the list
     */
    Collection<MetadataResolver> resolve(SamlRegisteredService service);

    /**
     * Supports this service?
     *
     * @param service the service
     * @return the boolean
     */
    boolean supports(SamlRegisteredService service);
}
