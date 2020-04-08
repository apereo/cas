package org.apereo.cas.monitor;

import lombok.val;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Monitors a {@link Cache} instance.
 * The accuracy of statistics is governed by the value of {@link Cache#getStatistics()}.
 * <p>NOTE: computation of highly accurate statistics is expensive.</p>
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 * @deprecated Since 6.2, due to Ehcache 2.x being unmaintained. Other registries are available, including Ehcache 3.x.
 */
@Deprecated(since = "6.2.0")
public class EhCacheHealthIndicator extends AbstractCacheHealthIndicator {

    /**
     * The Ticket granting tickets cache.
     */
    private final CacheManager ehcacheTicketsCache;

    public EhCacheHealthIndicator(final CacheManager ehcacheTicketsCache, final long evictionThreshold, final long threshold) {
        super(evictionThreshold, threshold);
        this.ehcacheTicketsCache = ehcacheTicketsCache;
    }

    @Override
    protected CacheStatistics[] getStatistics() {
        val list = Arrays.stream(this.ehcacheTicketsCache.getCacheNames())
            .map(c -> {
                val cache = this.ehcacheTicketsCache.getCache(c);
                return new EhCacheStatistics(cache);
            })
            .collect(Collectors.toList());
        return list.toArray(CacheStatistics[]::new);
    }
}
