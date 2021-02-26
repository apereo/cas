package org.apereo.cas.authentication.principal.cache;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalAttributesRepositoryCache;
import org.apereo.cas.authentication.principal.RegisteredServicePrincipalAttributesRepository;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.DigestUtils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.io.Closeable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link DefaultPrincipalAttributesRepositoryCache}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class DefaultPrincipalAttributesRepositoryCache implements PrincipalAttributesRepositoryCache, Closeable {
    private static final int DEFAULT_MAXIMUM_CACHE_SIZE = 1_000;

    private static final String DEFAULT_CACHE_EXPIRATION_UNIT = TimeUnit.HOURS.name();

    private final Map<String, Cache<String, Map<String, List<Object>>>> registeredServicesCache = new HashMap<>(0);

    /**
     * Build registered service cache key string.
     *
     * @param registeredService the registered service
     * @return the string
     */
    private static String buildRegisteredServiceCacheKey(final RegisteredService registeredService) {
        val key = registeredService.getId() + '@' + registeredService.getName();
        return DigestUtils.sha512(key);
    }

    /**
     * Initialize cache cache.
     *
     * @param repository the repository
     * @return the cache
     */
    private static Cache<String, Map<String, List<Object>>> initializeCache(final RegisteredServicePrincipalAttributesRepository repository) {
        val cachedRepository = CachingPrincipalAttributesRepository.class.cast(repository);
        val unit = TimeUnit.valueOf(StringUtils.defaultString(cachedRepository.getTimeUnit(), DEFAULT_CACHE_EXPIRATION_UNIT));
        return Caffeine.newBuilder()
            .initialCapacity(DEFAULT_MAXIMUM_CACHE_SIZE)
            .maximumSize(DEFAULT_MAXIMUM_CACHE_SIZE)
            .expireAfterWrite(cachedRepository.getExpiration(), unit)
            .build(s -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER));
    }

    @Override
    public void close() {
        invalidate();
    }

    /**
     * Invalidate all.
     */
    @Override
    public void invalidate() {
        registeredServicesCache.values().forEach(Cache::invalidateAll);
    }

    @Override
    public Map<String, List<Object>> fetchAttributes(final RegisteredService registeredService,
                                                     final RegisteredServicePrincipalAttributesRepository repository,
                                                     final Principal principal) {
        val cache = getRegisteredServiceCacheInstance(registeredService, repository);
        return cache.get(principal.getId(), s -> {
            LOGGER.debug("No cached attributes could be found for [{}]", principal.getId());
            return new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        });
    }

    @Override
    public void putAttributes(final RegisteredService registeredService,
                              final RegisteredServicePrincipalAttributesRepository repository,
                              final String id, final Map<String, List<Object>> attributes) {
        val cache = getRegisteredServiceCacheInstance(registeredService, repository);
        cache.put(id, attributes);
    }

    /**
     * Gets registered service cache instance.
     *
     * @param registeredService the registered service
     * @param repository        the repository
     * @return the registered service cache instance
     */
    private Cache<String, Map<String, List<Object>>> getRegisteredServiceCacheInstance(
        final RegisteredService registeredService, final RegisteredServicePrincipalAttributesRepository repository) {
        
        val key = buildRegisteredServiceCacheKey(registeredService);
        if (registeredServicesCache.containsKey(key)) {
            return registeredServicesCache.get(key);
        }
        val cache = initializeCache(repository);
        registeredServicesCache.put(key, cache);
        return cache;
    }
}
