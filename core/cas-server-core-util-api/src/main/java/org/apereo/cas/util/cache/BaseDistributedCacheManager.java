package org.apereo.cas.util.cache;

import module java.base;
import org.apereo.cas.util.PublisherIdentifier;

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
