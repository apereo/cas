package org.apereo.cas.monitor;

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
public class EhCacheMonitor extends AbstractCacheMonitor {

    /**
     * The Ticket granting tickets cache.
     */
    private final CacheManager ehcacheTicketsCache;

    /**
     * Instantiates a new Eh cache monitor.
     *
     * @param ehcacheTicketsCache the tickets cache
     */
    public EhCacheMonitor(final CacheManager ehcacheTicketsCache) {
        super(EhCacheMonitor.class.getSimpleName());
        this.ehcacheTicketsCache = ehcacheTicketsCache;
    }

    @Override
    protected CacheStatistics[] getStatistics() {
        final List<CacheStatistics> list = Arrays.stream(this.ehcacheTicketsCache.getCacheNames())
                .map(c -> {
                    final Cache cache = this.ehcacheTicketsCache.getCache(c);
                    return new EhCacheStatistics(cache);
                })
                .collect(Collectors.toList());

        return list.toArray(new CacheStatistics[]{});
    }
}
