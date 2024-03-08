package org.apereo.cas.support.saml.services.idp.metadata.cache;

import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;

import net.shibboleth.shared.resolver.CriteriaSet;

import java.util.Optional;


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
     * Bean name of the default implementation class.
     */
    String BEAN_NAME = "defaultSamlRegisteredServiceCachingMetadataResolver";

    /**
     * Resolve chaining metadata resolver.
     *
     * @param service     the service
     * @param criteriaSet the criteria set
     * @return the chaining metadata resolver
     * @throws Exception the exception
     */
    CachedMetadataResolverResult resolve(SamlRegisteredService service, CriteriaSet criteriaSet) throws Exception;

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

    /**
     * Attempts to fetch the entry from cache if present.
     * If the entry is not found, it will not attempt
     * to force resolve the entry and will return back empty.
     *
     * @param service     the service
     * @param criteriaSet the criteria set
     * @return the optional
     */
    Optional<CachedMetadataResolverResult> getIfPresent(SamlRegisteredService service, CriteriaSet criteriaSet);

    /**
     * Gets OpenSAML config bean.
     *
     * @return the OpenSAML config bean
     */
    OpenSamlConfigBean getOpenSamlConfigBean();
}
