package org.apereo.cas.services;

import org.apereo.cas.entity.SamlIdentityProviderEntity;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collection;

/**
 * This is {@link SamlIdentityProviderDiscoveryFeedService}.
 *
 * @author Sam Hough
 * @since 6.6.0
 */
public interface SamlIdentityProviderDiscoveryFeedService {

    /**
     * Available IdPs Entities (may not yet be built).
     *
     * @return the IdP Entities
     */
    Collection<SamlIdentityProviderEntity> getDiscoveryFeed();

    /**
     * The entityIDs of already built IdPs.
     *
     * @return the entityIDs
     */
    Collection<String> getEntityIds();

    /**
     * The provider for the given entityID.
     *
     * @param entityID            the entityID
     * @param httpServletRequest  the servlet request
     * @param httpServletResponse the servlet response
     * @return the provider
     */
    DelegatedClientIdentityProviderConfiguration getProvider(String entityID,
                                                             HttpServletRequest httpServletRequest,
                                                             HttpServletResponse httpServletResponse);

}
