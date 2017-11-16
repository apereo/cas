package org.apereo.cas.services;

import org.apereo.cas.DistributedCacheManager;

/**
 * This is {@link BaseDistributedCacheManager}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public abstract class BaseDistributedCacheManager<K, V, C> implements DistributedCacheManager<K, V, C> {
    @Override
    public void close() {
    }
    
}
