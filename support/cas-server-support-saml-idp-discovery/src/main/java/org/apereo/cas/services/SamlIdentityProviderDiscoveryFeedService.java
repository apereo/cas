package org.apereo.cas.services;

import org.apereo.cas.entity.SamlIdentityProviderEntity;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Collection;



/**
 * This is {@link SamlIdentityProviderDiscoveryFeedService}.
 *
 * @author Misagh Moayyed
 * @author Sam Hough
 * @since 6.6.0
 */
public interface SamlIdentityProviderDiscoveryFeedService {

    /**
     * @return available IdPs (may not yet be built)
     */
    Collection<SamlIdentityProviderEntity> getDiscoveryFeed();

    /**
     * @return the entityIDs of already built IdPs
     */
    Collection<String> getEntityIds();

    /**
     * @param entityID the entityID
     * @param httpServletRequest the servlet request
     * @param httpServletResponse the servlet response
     * @return the provider for the given entityID
     */
    DelegatedClientIdentityProviderConfiguration getProvider(String entityID,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse);

}
