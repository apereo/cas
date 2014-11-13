/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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

import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Factory to create principal object with attributes cached for a configurable period.
 * @author Misagh Moayyed
 * @since 4.1
 */
public final class CachingPrincipalAttributesRepository implements PrincipalAttributesRepository, Closeable {
    private static final long serialVersionUID = 6350244643948535906L;

    private static final TimeUnit DEFAULT_CACHE_EXPIRATION_UNIT = TimeUnit.HOURS;
    private static final long DEFAULT_CACHE_EXPIRATION_DURATION = 2;

    private static final Logger LOGGER = LoggerFactory.getLogger(CachingPrincipalAttributesRepository.class);

    private final IPersonAttributeDao attributeRepository;

    private final Cache<String, Map<String, Object>> cache;

    private final String cacheName = this.getClass().getSimpleName().concat(UUID.randomUUID().toString());

    /**
     * Instantiates a new caching attributes principal factory.
     * Caches the attributes based on duration units of {@link #DEFAULT_CACHE_EXPIRATION_DURATION}
     * and {@link #DEFAULT_CACHE_EXPIRATION_UNIT}.
     * @param attributeRepository the attribute repository
     */
    public CachingPrincipalAttributesRepository(final IPersonAttributeDao attributeRepository) {
        this(attributeRepository, createCacheConfiguration(
                new Duration(DEFAULT_CACHE_EXPIRATION_UNIT, DEFAULT_CACHE_EXPIRATION_DURATION)));

    }

    /**
     * Instantiates a new caching attributes principal factory.
     *
     * @param attributeRepository the attribute repository
     * @param timeUnit the time unit
     * @param expiryDuration the expiry duration
     */
    public CachingPrincipalAttributesRepository(final IPersonAttributeDao attributeRepository,
                                                final TimeUnit timeUnit,
                                                final long expiryDuration) {
        this(attributeRepository, createCacheConfiguration(new Duration(timeUnit, expiryDuration)));
    }

    /**
     * Instantiates a new caching attributes principal factory.
     *
     * @param attributeRepository the attribute repository
     * @param config the config
     */
    public CachingPrincipalAttributesRepository(final IPersonAttributeDao attributeRepository,
                                                final MutableConfiguration<String, Map<String, Object>> config) {
        this(attributeRepository, config, Caching.getCachingProvider().getCacheManager());
    }

    /**
     * Instantiates a new caching attributes principal factory.
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
     * Instantiates a new caching attributes principal factory.
     *
     * @param attributeRepository the attribute repository
     * @param config the config
     * @param manager the manager
     */
    public CachingPrincipalAttributesRepository(final IPersonAttributeDao attributeRepository,
                                                final MutableConfiguration<String, Map<String, Object>> config,
                                                final CacheManager manager) {
        this.attributeRepository = attributeRepository;
        this.cache = manager.createCache(this.cacheName, config);
    }

    /**
     * Instantiates a new caching attributes principal factory.
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
     * Gets cache configuration.
     *
     * @return the configuration
     */
    public MutableConfiguration<String, Map<String, Object>> getConfiguration() {
        return this.cache.getConfiguration(MutableConfiguration.class);
    }

    /**
     * Prep cache configuration.
     *
     * @param expiryDuration the expiry duration
     * @return the mutable configuration
     */
    protected static MutableConfiguration<String, Map<String, Object>> createCacheConfiguration(final Duration expiryDuration) {
        final MutableConfiguration<String, Map<String, Object>> config = new MutableConfiguration<String, Map<String, Object>>();
        config.setStatisticsEnabled(true);
        config.setManagementEnabled(true);
        config.setStoreByValue(true);
        config.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(expiryDuration));
        return config;
    }

    @Override
    public void setAttributes(final String id, final Map<String, Object> attributes) {
        synchronized (this.cache) {
            this.cache.put(id, attributes);
        }
    }

    @Override
    public Map<String, Object> getAttributes(final String id) {
        Map<String, Object> attributes = this.cache.get(id);
        if (attributes == null) {
            attributes = convertPersonAttributesToPrincipalAttributes(id);
            setAttributes(id, attributes);
            return attributes;
        }
        return attributes;
    }

    /**
     * Convert person attributes to principal attributes.
     * Obtains attributes first from the repository by calling
     * {@link org.jasig.services.persondir.IPersonAttributeDao#getPerson(String)}
     * and converts the results into a map of attributes that CAS can understand.
     *
     * @param id the person id to locate in the attribute repository
     * @return the map of principal attributes
     */
    private Map<String, Object> convertPersonAttributesToPrincipalAttributes(final String id) {

        final IPersonAttributes attrs = this.attributeRepository.getPerson(id);
        if (attrs == null) {
            return Collections.emptyMap();
        }

        final Map<String, List<Object>> attributes = attrs.getAttributes();
        if (attributes == null) {
            return Collections.emptyMap();
        }

        final Map<String, Object> convertedAttributes = new HashMap<String, Object>();
        for (final String key : attributes.keySet()) {
            final List<Object> values = attributes.get(key);
            convertedAttributes.put(key, values.size() == 1 ? values.get(0) : values);
        }
        return convertedAttributes;
    }

    @Override
    public void close() throws IOException {
        this.cache.close();
        this.cache.getCacheManager().close();
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
