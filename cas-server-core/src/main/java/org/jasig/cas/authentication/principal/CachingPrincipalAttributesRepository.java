/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.authentication.principal;

import org.jasig.cas.util.PrincipalUtils;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Factory to create principal object with attributes cached for a configurable period.
 * @author Misagh Moayyed
 * @since 4.1
 */
public final class CachingPrincipalAttributesRepository implements PrincipalAttributesRepository {
    private static final long serialVersionUID = 6350244643948535906L;

    private static final String ATTRIBUTES_CACHE_KEY = CachingPrincipalAttributesRepository.class.getSimpleName();

    private static final Logger LOGGER = LoggerFactory.getLogger(CachingPrincipalAttributesRepository.class);

    private final IPersonAttributeDao attributeRepository;

    private final Cache<String, Map<String, Object>> cache;

    /**
     * Instantiates a new Uncached attributes principal factory.
     *
     * @param attributeRepository the attribute repository
     */
    public CachingPrincipalAttributesRepository(final IPersonAttributeDao attributeRepository) {
        this(attributeRepository, prepConfiguration(new Duration(TimeUnit.HOURS, 8)));
    }

    /**
     * Instantiates a new Uncached attributes principal factory.
     *
     * @param attributeRepository the attribute repository
     * @param timeUnit the time unit
     * @param expiryDuration the expiry duration
     */
    public CachingPrincipalAttributesRepository(final IPersonAttributeDao attributeRepository,
                                                final TimeUnit timeUnit,
                                                final long expiryDuration) {
        this(attributeRepository, prepConfiguration(new Duration(timeUnit, expiryDuration)));

    }

    /**
     * Instantiates a new Uncached attributes principal factory.
     *
     * @param attributeRepository the attribute repository
     * @param config the config
     */
    public CachingPrincipalAttributesRepository(final IPersonAttributeDao attributeRepository,
                                                final MutableConfiguration<String, Map<String, Object>> config) {
        this(attributeRepository, config, Caching.getCachingProvider().getCacheManager());
    }

    /**
     * Instantiates a new Uncached attributes principal factory.
     *
     * @param attributeRepository the attribute repository
     * @param config the config
     * @param cacheProviderFullClassName the cache provider full class name
     */
    public CachingPrincipalAttributesRepository(final IPersonAttributeDao attributeRepository,
                                                final MutableConfiguration<String, Map<String, Object>> config,
                                                final String cacheProviderFullClassName) {
        this(attributeRepository, config,
                Caching.getCachingProvider(cacheProviderFullClassName).getCacheManager());
    }

    /**
     * Instantiates a new Uncached attributes principal factory.
     *
     * @param attributeRepository the attribute repository
     * @param config the config
     * @param manager the manager
     */
    public CachingPrincipalAttributesRepository(final IPersonAttributeDao attributeRepository,
                                                final MutableConfiguration<String, Map<String, Object>> config,
                                                final CacheManager manager) {
        this(attributeRepository,
                manager.createCache(CachingPrincipalAttributesRepository.class.getSimpleName(), config));
    }

    /**
     * Instantiates a new Uncached attributes principal factory.
     *
     * @param attributeRepository the attribute repository
     * @param cache the cache
     */
    public CachingPrincipalAttributesRepository(final IPersonAttributeDao attributeRepository,
                                                final Cache<String, Map<String, Object>> cache) {
        this.attributeRepository = attributeRepository;
        this.cache = cache;
    }

    /**
     * Gets attribute repository.
     *
     * @return the attribute repository
     */
    public IPersonAttributeDao getAttributeRepository() {
        return this.attributeRepository;
    }

    /**
     * Prep cache configuration.
     *
     * @param expiryDuration the expiry duration
     * @return the mutable configuration
     */
    private static MutableConfiguration<String, Map<String, Object>> prepConfiguration(final Duration expiryDuration) {
        final MutableConfiguration<String, Map<String, Object>> config = new MutableConfiguration<String, Map<String, Object>>();
        config.setStatisticsEnabled(true);
        config.setManagementEnabled(true);
        config.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(expiryDuration));
        return config;
    }

    @Override
    public void setAttributes(final Map<String, Object> attributes) {
        this.cache.put(ATTRIBUTES_CACHE_KEY, attributes);
    }

    @Override
    public Map<String, Object> getAttributes(final String id) {
        Map<String, Object> attributes = this.cache.get(id);
        if (attributes == null) {
            attributes = PrincipalUtils.convertPersonAttributesToPrincipalAttributes(id, this.attributeRepository);
            setAttributes(attributes);
            return attributes;
        }
        return attributes;
    }
}
