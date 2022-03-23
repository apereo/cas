package org.apereo.cas.services;

import java.util.Collection;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apereo.cas.entity.SamlIdentityProviderEntity;

import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;

public interface SamlIdentityProviderDiscoveryFeedService {

	Collection<SamlIdentityProviderEntity> getDiscoveryFeed();

	Collection<String> getEntityIds();
	
	DelegatedClientIdentityProviderConfiguration getProvider(final String entityID,
			final HttpServletRequest httpServletRequest,
			final HttpServletResponse httpServletResponse);

}
