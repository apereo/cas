package org.apereo.cas.services;

import module java.base;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

/**
 * This is {@link CacheableServicesManager}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public interface CacheableServicesManager extends ServicesManager {

    /**
     * Gets cached registered services.
     *
     * @return the cached registered services
     */
    Map<Long, RegisteredService> getCachedRegisteredServices();

    /**
     * Gets cached registered services size.
     *
     * @return the cached registered services size
     */
    long getCachedRegisteredServicesSize();

    /**
     * Clean registered services cache.
     */
    void cleanRegisteredServicesCache();

    /**
     * Cache service.
     *
     * @param service the service
     */
    void cacheRegisteredService(RegisteredService service);

    /**
     * Cache registered services map.
     *
     * @param services the services
     * @return the map
     */
    @CanIgnoreReturnValue
    Map<Long, RegisteredService> cacheRegisteredServices(Map<Long, RegisteredService> services);

    /**
     * Remove registered service.
     *
     * @param service the service
     */
    void removeRegisteredServiceFromCache(RegisteredService service);

    /**
     * Remove registered services from cache.
     */
    void removeRegisteredServicesFromCache();

    /**
     * Find cached registered service.
     *
     * @param key   the key
     * @param clazz the clazz
     * @return the registered service
     */
    RegisteredService findCachedRegisteredService(Long key, Class<? extends RegisteredService> clazz);
}
