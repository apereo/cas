package org.apereo.cas.services;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.DistributedCacheManager;
import org.apereo.cas.DistributedCacheObject;

import java.io.Serializable;

/**
 * This is {@link BaseDistributedCacheManager}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public abstract class BaseDistributedCacheManager<K extends Serializable, V extends DistributedCacheObject> implements DistributedCacheManager<K, V> {
    @Override
    public void close() {
    }
}
