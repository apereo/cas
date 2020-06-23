package org.apereo.cas.services;

import org.apereo.cas.util.cache.DistributedCacheManager;
import org.apereo.cas.util.cache.DistributedCacheObject;

import java.io.Serializable;

/**
 * This is {@link BaseDistributedCacheManager}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public abstract class BaseDistributedCacheManager<K extends Serializable, V extends DistributedCacheObject> implements DistributedCacheManager<K, V> {

    /**
     * Gets key.
     *
     * @param service the service
     * @return the key
     */
    public static String buildKey(final RegisteredService service) {
        return service.getId() + ";" + service.getName() + ';' + service.getServiceId();
    }
    
    @Override
    public void close() {
    }
}
