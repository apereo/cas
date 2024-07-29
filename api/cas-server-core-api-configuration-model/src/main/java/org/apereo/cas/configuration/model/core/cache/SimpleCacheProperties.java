package org.apereo.cas.configuration.model.core.cache;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link SimpleCacheProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-core-util", automated = true)
@Getter
@Setter
@Accessors(chain = true)

public class SimpleCacheProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -168826011744304210L;

    /**
     * This cache size specifies the maximum number of entries the cache may contain.
     * Note that the cache may evict an entry before this limit is exceeded or temporarily
     * exceed the threshold while evicting. As the cache size grows close to the maximum,
     * the cache evicts entries that are less likely to be used again. For example, the
     * cache may evict an entry because it hasn't been used recently or very often.
     * Note: to disable the cache, you may choose a cache size of {@code 0}.
     */
    private long cacheSize = 10_000L;

    /**
     * This cache capacity sets the minimum total size for the internal data structures.
     * Providing a large enough estimate at construction time avoids the need for expensive resizing
     * operations later, but setting this value unnecessarily high wastes memory.
     */
    private int initialCapacity = 1_000;
}
