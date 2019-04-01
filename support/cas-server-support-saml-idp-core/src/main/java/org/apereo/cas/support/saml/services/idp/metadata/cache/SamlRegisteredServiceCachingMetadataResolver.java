package org.apereo.cas.support.saml.services.idp.metadata.cache;

import org.apereo.cas.support.saml.services.SamlRegisteredService;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.opensaml.saml.metadata.resolver.MetadataResolver;

/**
 * This is {@link SamlRegisteredServiceCachingMetadataResolver}
 * that defines how metadata is to be resolved and cached for a given saml
 * registered service.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface SamlRegisteredServiceCachingMetadataResolver {

    /**
     * Resolve chaining metadata resolver.
     *
     * @param service     the service
     * @param criteriaSet the criteria set
     * @return the chaining metadata resolver
     */
    MetadataResolver resolve(SamlRegisteredService service, CriteriaSet criteriaSet);

    /**
     * Invalid and clean the result of all previous operations.
     * Invocation of this method is expected to force a clean
     * resolution of the metadata for all follow-up requests, disregarding
     * any and all cached results.
     */
    void invalidate();

    /**
     * Invalid and clean the result of previous operations for the given service.
     * Invocation of this method is expected to force a clean
     * resolution of the metadata for all follow-up requests that apply to the given service (relying party),
     * disregarding any and all cached results.
     *
     * @param service     the service
     * @param criteriaSet the criteria set
     */
    void invalidate(SamlRegisteredService service, CriteriaSet criteriaSet);
}
