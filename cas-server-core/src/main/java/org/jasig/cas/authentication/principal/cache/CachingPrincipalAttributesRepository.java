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
package org.jasig.cas.authentication.principal.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.services.persondir.IPersonAttributeDao;

import javax.annotation.PreDestroy;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper around an attribute repository where attributes cached for a configurable period.
 * @author Misagh Moayyed
 * @since 4.1
 */
public final class CachingPrincipalAttributesRepository extends AbstractPrincipalAttributesRepository {
    private static final long serialVersionUID = 6350244643948535906L;

    private final Cache<String, Map<String, Object>> cache;

    private final String cacheName = this.getClass().getSimpleName().concat(UUID.randomUUID().toString());

    private Duration duration;

    /**
     * Init the caching repository, solely used for serialization purposes
     * and nothing else.
     */
    private CachingPrincipalAttributesRepository() {
        super();
        this.cache = null;
        this.duration = null;
    }

    /**
     * Instantiates a new caching attributes principal factory.
     * Caches the attributes based on duration units of {@link #DEFAULT_CACHE_EXPIRATION_DURATION}
     * and {@link #DEFAULT_CACHE_EXPIRATION_UNIT}.
     * @param attributeRepository the attribute repository
     */
    public CachingPrincipalAttributesRepository(final IPersonAttributeDao attributeRepository) {
        this(attributeRepository, DEFAULT_CACHE_EXPIRATION_DURATION);
    }

    /**
     * Instantiates a new caching attributes principal factory.
     * Caches the attributes based on duration units of {@link #DEFAULT_CACHE_EXPIRATION_DURATION}
     * and the given time.
     * @param attributeRepository the attribute repository
     * @param expiryDuration the expiry duration based on the unit of {@link #DEFAULT_CACHE_EXPIRATION_DURATION}
     */
    public CachingPrincipalAttributesRepository(final IPersonAttributeDao attributeRepository, final long expiryDuration) {
        this(attributeRepository, DEFAULT_CACHE_EXPIRATION_UNIT, expiryDuration);
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
        this(attributeRepository, new Duration(timeUnit, expiryDuration));
    }

    /**
     * Instantiates a new caching attributes principal factory.
     *
     * @param attributeRepository the attribute repository
     * @param duration the duration
     */
    private CachingPrincipalAttributesRepository(final IPersonAttributeDao attributeRepository,
                                                 final Duration duration) {
        this(attributeRepository, createCacheConfiguration(duration));
        this.duration = duration;
    }

    /**
     * Instantiates a new caching attributes principal factory.
     *
     * @param attributeRepository the attribute repository
     * @param config the config
     */
    private CachingPrincipalAttributesRepository(final IPersonAttributeDao attributeRepository,
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
    private CachingPrincipalAttributesRepository(final IPersonAttributeDao attributeRepository,
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
    private CachingPrincipalAttributesRepository(final IPersonAttributeDao attributeRepository,
                                                 final MutableConfiguration<String, Map<String, Object>> config,
                                                 final CacheManager manager) {
        super(attributeRepository);
        this.cache = manager.createCache(this.cacheName, config);
    }

    /**
     * Instantiates a new caching attributes principal factory.
     *
     * @param attributeRepository the attribute repository
     * @param cache the cache
     */
    private CachingPrincipalAttributesRepository(final IPersonAttributeDao attributeRepository,
                                                 final Cache<String, Map<String, Object>> cache) {
        super(attributeRepository);
        this.cache = cache;
    }

    public Duration getDuration() {
        return this.duration;
    }

    /**
     * Gets cache configuration.
     *
     * @return the configuration
     */
    @JsonIgnore
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
        final MutableConfiguration<String, Map<String, Object>> config = new MutableConfiguration<>();
        config.setStatisticsEnabled(true);
        config.setManagementEnabled(true);
        config.setStoreByValue(true);
        config.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(expiryDuration));
        return config;
    }

    /**
     * Set/add the received attributes into the cache.
     * @param id the principal id that controls the grouping of the attributes in the cache.
     * @param attributes principal attributes to add to the cache
     */
    @Override
    protected void addPrincipalAttributesIntoCache(final String id, final Map<String, Object> attributes) {
        synchronized (this.cache) {
            if (attributes.isEmpty()) {
                this.cache.remove(id);
                logger.debug("No attributes are provided, so removed principal id [{}] from the cache", id);
            } else {
                this.cache.put(id, attributes);
                logger.debug("Cached [{}] attributes for the principal id [{}]", attributes.size(), id);
            }
        }
    }

    @Override
    protected Map<String, Object> getPrincipalAttributesFromCache(final Principal p) {
        return this.cache.get(p.getId());
    }

    @Override
    @PreDestroy
    public void close() throws IOException {
        this.cache.close();
        this.cache.getCacheManager().close();
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper((super.toString()))
                .append("cache", cache)
                .append("cacheName", cacheName)
                .append("durationTimeUnit", duration.getTimeUnit())
                .append("durationAmount", duration.getDurationAmount())
                .toString();
    }


    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final CachingPrincipalAttributesRepository rhs = (CachingPrincipalAttributesRepository) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        return builder
                .append(this.duration, rhs.duration)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 133)
                .append(duration)
                .toHashCode();
    }

}
