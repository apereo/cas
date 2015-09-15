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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.services.persondir.IPersonAttributeDao;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper around an attribute repository where attributes cached for a configurable period
 * based on google guava's caching library.
 * @author Misagh Moayyed
 * @since 4.2
 */
public final class GuavaCachingPrincipalAttributesRepository extends AbstractPrincipalAttributesRepository {
    private static final long serialVersionUID = 6350244643948535906L;
    private static final long DEFAULT_MAXIMUM_CACHE_SIZE = 1000;

    private final transient Cache<String, Map<String, Object>> cache;

    private final long expiration;
    private final TimeUnit timeUnit;

    /**
     * Instantiates a new caching attributes principal factory.
     * Caches the attributes based on duration units of {@link #DEFAULT_CACHE_EXPIRATION_DURATION}
     * and the given time.
     * @param attributeRepository the attribute repository
     * @param expiryDuration the expiry duration based on the unit of {@link #DEFAULT_CACHE_EXPIRATION_DURATION}
     */
    public GuavaCachingPrincipalAttributesRepository(final IPersonAttributeDao attributeRepository,
                                                     final long expiryDuration) {
        this(attributeRepository, DEFAULT_CACHE_EXPIRATION_UNIT, expiryDuration);
    }

    /**
     * Instantiates a new caching attributes principal factory.
     * Sets the default cache size to {@link #DEFAULT_MAXIMUM_CACHE_SIZE}.
     * @param attributeRepository the attribute repository
     * @param timeUnit the time unit
     * @param expiryDuration the expiry duration
     */
    public GuavaCachingPrincipalAttributesRepository(final IPersonAttributeDao attributeRepository,
                                                final TimeUnit timeUnit,
                                                final long expiryDuration) {
        this(attributeRepository, DEFAULT_MAXIMUM_CACHE_SIZE, timeUnit, expiryDuration);
    }

    /**
     * Instantiates a new caching attributes principal factory.
     * @param attributeRepository the attribute repository
     * @param maxCacheSize the max cache size
     * @param timeUnit the time unit
     * @param expiryDuration the expiry duration
     */
    public GuavaCachingPrincipalAttributesRepository(final IPersonAttributeDao attributeRepository,
                                                     final long maxCacheSize,
                                                     final TimeUnit timeUnit,
                                                     final long expiryDuration) {
        super(attributeRepository);
        this.cache = CacheBuilder.newBuilder().maximumSize(maxCacheSize)
                .expireAfterWrite(expiryDuration, timeUnit).build();
        this.expiration = expiryDuration;
        this.timeUnit = timeUnit;
    }

    @Override
    protected void addPrincipalAttributesIntoCache(final String id, final Map<String, Object> attributes) {
        this.cache.put(id, attributes);
        logger.debug("Cached attributes for {}", id);
    }

    @Override
    protected Map<String, Object> getPrincipalAttributesFromCache(final Principal p) {
        try {
            return this.cache.get(p.getId(), new Callable<Map<String, Object>>() {
                @Override
                public Map<String, Object> call() throws Exception {
                    logger.debug("No cached attributes could be found for {}", p.getId());
                    return new HashMap<>();
                }
            });
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        this.cache.cleanUp();
    }

    public long getExpiration() {
        return expiration;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
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
        final GuavaCachingPrincipalAttributesRepository rhs = (GuavaCachingPrincipalAttributesRepository) obj;
        return new EqualsBuilder()
                .append(this.timeUnit, rhs.timeUnit)
                .append(this.expiration, rhs.expiration)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 133)
                .append(this.timeUnit)
                .append(this.expiration)
                .toHashCode();
    }
}
