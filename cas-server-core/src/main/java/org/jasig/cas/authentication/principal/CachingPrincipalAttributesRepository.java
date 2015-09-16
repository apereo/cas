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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.merger.IAttributeMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.validation.constraints.NotNull;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper around an attribute repository where attributes cached for a configurable period.
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

    private Duration duration;

    /**
     * The merging strategy that deals with existing principal attributes
     * and those that are retrieved from the source. By default, existing attributes
     * are ignored and the source is always consulted.
     */
    private IAttributeMerger mergingStrategy;

    /**
     * Init the caching repository, solely used for serialization purposes
     * and nothing else.
     */
    private CachingPrincipalAttributesRepository() {
        this.attributeRepository = null;
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
        this.attributeRepository = attributeRepository;
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
     * The merging strategy that deals with existing principal attributes
     * and those that are retrieved from the source. By default, existing attributes
     * are ignored and the source is always consulted.
     * @param mergingStrategy the strategy to use for conflicts
     */
    public void setMergingStrategy(final IAttributeMerger mergingStrategy) {
        this.mergingStrategy = mergingStrategy;
    }

    public IAttributeMerger getMergingStrategy() {
        return mergingStrategy;
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
    private void addPrincipalAttributesIntoCache(final String id, final Map<String, Object> attributes) {
        synchronized (this.cache) {
            if (attributes.isEmpty()) {

                this.cache.remove(id);
                LOGGER.debug("No attributes are provided, so removed principal id [{}] from the cache", id);
            } else {
                this.cache.put(id, attributes);
                LOGGER.debug("Cached [{}] attributes for the principal id [{}]", attributes.size(), id);
            }
        }
    }

    @Override
    public Map<String, Object> getAttributes(@NotNull final Principal p) {
        final Map<String, Object> cachedAttributes = this.cache.get(p.getId());
        if (cachedAttributes != null) {
            LOGGER.debug("Found [{}] cached attributes for principal [{}]", cachedAttributes.size(), p.getId());
            return cachedAttributes;
        }

        final Map<String, List<Object>> sourceAttributes = retrievePersonAttributesToPrincipalAttributes(p.getId());
        LOGGER.debug("Found [{}] attributes for principal [{}] from the attribute repository.",
                sourceAttributes.size(), p.getId());

        if (this.mergingStrategy == null) {
            LOGGER.debug("No merging strategy found, so attributes retrieved from the repository will be used instead.");
            final Map<String, Object> finalAttributes = convertPersonAttributesToPrincipalAttributes(sourceAttributes);
            addPrincipalAttributesIntoCache(p.getId(), finalAttributes);
            return finalAttributes;
        }

        final Map<String, List<Object>> principalAttributes = convertPrincipalAttributesToPersonAttributes(p);

        LOGGER.debug("Merging current principal attributes with that of the repository via strategy [{}]",
                this.mergingStrategy.getClass().getSimpleName());
        final Map<String, List<Object>> mergedAttributes =
                this.mergingStrategy.mergeAttributes(principalAttributes, sourceAttributes);

        final Map<String, Object> finalAttributes = convertPersonAttributesToPrincipalAttributes(mergedAttributes);
        addPrincipalAttributesIntoCache(p.getId(), finalAttributes);
        return finalAttributes;

    }

    /***
     * Convert principal attributes to person attributes.
     * @param p  the principal carrying attributes
     * @return person attributes
     */
    private Map<String, List<Object>> convertPrincipalAttributesToPersonAttributes(final Principal p) {
        final Map<String, List<Object>> convertedAttributes = new HashMap<>(p.getAttributes().size());
        final Map<String, Object> principalAttributes = p.getAttributes();

        for (final Map.Entry<String, Object> entry : principalAttributes.entrySet()) {
            final Object values = entry.getValue();
            final String key = entry.getKey();
            if (values instanceof List) {
                convertedAttributes.put(key, (List) values);
            } else {
                convertedAttributes.put(key, Collections.singletonList(values));
            }
        }
        return convertedAttributes;
    }

    /**
     * Convert person attributes to principal attributes.
     * @param attributes person attributes
     * @return principal attributes
     */
    private Map<String, Object> convertPersonAttributesToPrincipalAttributes(final Map<String, List<Object>> attributes) {
        final Map<String, Object> convertedAttributes = new HashMap<>();
        for (final Map.Entry<String, List<Object>> entry : attributes.entrySet()) {
            final List<Object> values = entry.getValue();
            convertedAttributes.put(entry.getKey(), values.size() == 1 ? values.get(0) : values);
        }
        return convertedAttributes;
    }

    /**
     * Obtains attributes first from the repository by calling
     * {@link org.jasig.services.persondir.IPersonAttributeDao#getPerson(String)}.
     *
     * @param id the person id to locate in the attribute repository
     * @return the map of attributes
     */
    private Map<String, List<Object>> retrievePersonAttributesToPrincipalAttributes(final String id) {

        final IPersonAttributes attrs = this.attributeRepository.getPerson(id);
        if (attrs == null) {
            LOGGER.debug("Could not find principal [{}] in the repository so no attributes are returned.", id);
            return Collections.emptyMap();
        }

        final Map<String, List<Object>> attributes = attrs.getAttributes();
        if (attributes == null) {
            LOGGER.debug("Principal [{}] has no attributes and so none are returned.", id);
            return Collections.emptyMap();
        }
        return attributes;
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
                .append("attributeRepository", attributeRepository)
                .append("cache", cache)
                .append("cacheName", cacheName)
                .append("durationTimeUnit", duration.getTimeUnit())
                .append("durationAmount", duration.getDurationAmount())
                .append("mergingStrategy", mergingStrategy)
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
