package org.apereo.cas.authentication.principal.cache;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.DigestUtils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link PrincipalAttributesRepositoryCache}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class PrincipalAttributesRepositoryCache implements Closeable {
    private static final int DEFAULT_MAXIMUM_CACHE_SIZE = 1000;
    private static final String DEFAULT_CACHE_EXPIRATION_UNIT = TimeUnit.HOURS.name();

    private final Map<String, Cache<String, Map<String, Object>>> registeredServicesCache = new HashMap<>();

    @Override
    public void close() {
        registeredServicesCache.values().forEach(Cache::cleanUp);
    }

    /**
     * Gets cached attributes. Locate the cache for the service first.
     *
     * @param registeredService the registered service
     * @param repository        the repository
     * @param principal         the principal
     * @return the cached attributes for
     */
    public Map<String, Object> getCachedAttributesFor(final RegisteredService registeredService,
                                                      final CachingPrincipalAttributesRepository repository,
                                                      final Principal principal) {
        val cache = getRegisteredServiceCacheInstance(registeredService, repository);
        return cache.get(principal.getId(), s -> {
            LOGGER.debug("No cached attributes could be found for [{}]", principal.getId());
            return new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        });
    }

    /**
     * Put cached attributes.
     *
     * @param registeredService the registered service
     * @param repository        the repository
     * @param id                the id
     * @param attributes        the attributes
     */
    public void putCachedAttributesFor(final RegisteredService registeredService,
                                       final CachingPrincipalAttributesRepository repository,
                                       final String id, final Map<String, Object> attributes) {
        val cache = getRegisteredServiceCacheInstance(registeredService, repository);
        cache.put(id, attributes);
    }

    private static String buildRegisteredServiceCacheKey(final RegisteredService registeredService) {
        val key = registeredService.getId() + '@' + registeredService.getName();
        return DigestUtils.sha512(key);
    }

    private Cache<String, Map<String, Object>> getRegisteredServiceCacheInstance(final RegisteredService registeredService,
                                                                                 final CachingPrincipalAttributesRepository repository) {
        val key = buildRegisteredServiceCacheKey(registeredService);
        if (registeredServicesCache.containsKey(key)) {
            return registeredServicesCache.get(key);
        }
        val cache = initializeCache(repository);
        registeredServicesCache.put(key, cache);
        return cache;
    }

    private static Cache<String, Map<String, Object>> initializeCache(final CachingPrincipalAttributesRepository repository) {
        val unit = TimeUnit.valueOf(StringUtils.defaultString(repository.getTimeUnit(), DEFAULT_CACHE_EXPIRATION_UNIT));
        return Caffeine.newBuilder()
            .initialCapacity(DEFAULT_MAXIMUM_CACHE_SIZE)
            .maximumSize(DEFAULT_MAXIMUM_CACHE_SIZE)
            .expireAfterWrite(repository.getExpiration(), unit)
            .build(s -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER));
    }
}
