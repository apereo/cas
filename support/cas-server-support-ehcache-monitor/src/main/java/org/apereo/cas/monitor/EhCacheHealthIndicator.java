package org.apereo.cas.monitor;

import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Monitors a {@link Cache} instance.
 * The accuracy of statistics is governed by the value of {@link Cache#getStatistics()}.
 * <p>NOTE: computation of highly accurate statistics is expensive.</p>
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
@Slf4j
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
        final List<CacheStatistics> list = Arrays.stream(this.ehcacheTicketsCache.getCacheNames())
            .map(c -> {
                final var cache = this.ehcacheTicketsCache.getCache(c);
                return new EhCacheStatistics(cache);
            })
            .collect(Collectors.toList());

        return list.toArray(new CacheStatistics[]{});
    }
}
