package org.apereo.cas.util.cache;

import org.apereo.cas.util.PublisherIdentifier;

import java.io.Serializable;

/**
 * This is {@link BaseDistributedCacheManager}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public abstract class BaseDistributedCacheManager<K extends Serializable, V extends DistributedCacheObject>
    implements DistributedCacheManager<K, V, PublisherIdentifier> {
    @Override
    public void close() {
    }
}
