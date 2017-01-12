package org.apereo.cas.monitor;

import net.sf.ehcache.Cache;

import java.util.ArrayList;
import java.util.List;

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
    private final Cache ehcacheTicketsCache;

    /**
     * Instantiates a new Eh cache monitor.
     *
     * @param ehcacheTicketsCache the tickets cache
     */
    public EhCacheMonitor(final Cache ehcacheTicketsCache) {
        super(EhCacheMonitor.class.getSimpleName());
        this.ehcacheTicketsCache = ehcacheTicketsCache;
    }

    @Override
    protected CacheStatistics[] getStatistics() {
        final List<CacheStatistics> list = new ArrayList<>();
        if (this.ehcacheTicketsCache != null) {
            list.add(new EhCacheStatistics(this.ehcacheTicketsCache));
        }
        return list.toArray(new CacheStatistics[]{});
    }
}
