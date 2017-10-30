package org.apereo.cas.authentication.principal.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apereo.cas.authentication.principal.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Transient;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper around an attribute repository where attributes cached for a configurable period
 * based on google guava's caching library.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class CachingPrincipalAttributesRepository extends AbstractPrincipalAttributesRepository {
    private static final long serialVersionUID = 6350244643948535906L;
    private static final long DEFAULT_MAXIMUM_CACHE_SIZE = 1000;

    private static final Logger LOGGER = LoggerFactory.getLogger(CachingPrincipalAttributesRepository.class);
    
    @JsonIgnore
    @Transient
    private final transient Cache<String, Map<String, Object>> cache;
    
    @JsonIgnore
    @Transient
    private final transient PrincipalAttributesCacheLoader cacheLoader = new PrincipalAttributesCacheLoader();

    private long maxCacheSize = DEFAULT_MAXIMUM_CACHE_SIZE;

    /**
     * Used for serialization only.
     */
    private CachingPrincipalAttributesRepository() {
        super();
        this.cache = Caffeine.newBuilder().maximumSize(this.maxCacheSize)
                .expireAfterWrite(getExpiration(), TimeUnit.valueOf(getTimeUnit())).build(this.cacheLoader);
    }

    /**
     * Instantiates a new caching attributes principal factory.
     * Sets the default cache size to {@link #DEFAULT_MAXIMUM_CACHE_SIZE}.
     *
     * @param timeUnit       the time unit
     * @param expiryDuration the expiry duration
     */
    public CachingPrincipalAttributesRepository(final String timeUnit, final long expiryDuration) {
        this(DEFAULT_MAXIMUM_CACHE_SIZE, timeUnit, expiryDuration);
    }

    /**
     * Instantiates a new caching attributes principal factory.
     *
     * @param maxCacheSize   the max cache size
     * @param timeUnit       the time unit
     * @param expiryDuration the expiry duration
     */
    public CachingPrincipalAttributesRepository(final long maxCacheSize,
                                                final String timeUnit,
                                                final long expiryDuration) {
        super(expiryDuration, timeUnit);
        this.maxCacheSize = maxCacheSize;
        this.cache = Caffeine.newBuilder().maximumSize(maxCacheSize)
                .expireAfterWrite(getExpiration(), TimeUnit.valueOf(getTimeUnit())).build(this.cacheLoader);
    }

    @Override
    protected void addPrincipalAttributes(final String id, final Map<String, Object> attributes) {
        this.cache.put(id, attributes);
        LOGGER.debug("Cached attributes for [{}]", id);
    }

    @Override
    protected Map<String, Object> getPrincipalAttributes(final Principal p) {
        try {
            return this.cache.get(p.getId(), s -> {
                LOGGER.debug("No cached attributes could be found for [{}]", p.getId());
                return new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            });
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new HashMap<>(0);
    }

    @Override
    public void close() {
        this.cache.cleanUp();
    }

    private static class PrincipalAttributesCacheLoader implements CacheLoader<String, Map<String, Object>> {
        @Override
        public Map<String, Object> load(final String key) {
            return new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        }
    }
}
