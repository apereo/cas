package org.jasig.cas.authentication.principal.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import org.jasig.cas.authentication.principal.Principal;

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
public final class CachingPrincipalAttributesRepository extends AbstractPrincipalAttributesRepository {
    private static final long serialVersionUID = 6350244643948535906L;
    private static final long DEFAULT_MAXIMUM_CACHE_SIZE = 1000;

    private final transient Cache<String, Map<String, Object>> cache;
    private final transient PrincipalAttributesCacheLoader cacheLoader =
            new PrincipalAttributesCacheLoader();

    private long maxCacheSize = DEFAULT_MAXIMUM_CACHE_SIZE;

    /**
     * Used for serialization only.
     */
    private CachingPrincipalAttributesRepository() {
        super();
        this.cache = CacheBuilder.newBuilder().maximumSize(this.maxCacheSize)
                .expireAfterWrite(this.expiration, this.timeUnit).build(this.cacheLoader);
    }

    /**
     * Instantiates a new caching attributes principal factory.
     * Sets the default cache size to {@link #DEFAULT_MAXIMUM_CACHE_SIZE}.
     * @param timeUnit the time unit
     * @param expiryDuration the expiry duration
     */
    public CachingPrincipalAttributesRepository(final TimeUnit timeUnit,
                                                final long expiryDuration) {
        this(DEFAULT_MAXIMUM_CACHE_SIZE, timeUnit, expiryDuration);
    }

    /**
     * Instantiates a new caching attributes principal factory.
     * @param maxCacheSize the max cache size
     * @param timeUnit the time unit
     * @param expiryDuration the expiry duration
     */
    public CachingPrincipalAttributesRepository(final long maxCacheSize,
                                                final TimeUnit timeUnit,
                                                final long expiryDuration) {
        super(expiryDuration, timeUnit);
        this.maxCacheSize = maxCacheSize;

        this.cache = CacheBuilder.newBuilder().maximumSize(maxCacheSize)
                .expireAfterWrite(expiryDuration, timeUnit).build(this.cacheLoader);
    }

    @Override
    protected void addPrincipalAttributes(final String id, final Map<String, Object> attributes) {
        this.cache.put(id, attributes);
        logger.debug("Cached attributes for {}", id);
    }

    @Override
    protected Map<String, Object> getPrincipalAttributes(final Principal p) {
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

    private static class PrincipalAttributesCacheLoader extends CacheLoader<String, Map<String, Object>> {
        @Override
        public Map<String, Object> load(final String key) throws Exception {
            return new HashMap<>();
        }
    }
}
