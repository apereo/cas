package org.jasig.cas.monitor;

import net.sf.ehcache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

/**
 * Monitors a {@link Cache} instance.
 * The accuracy of statistics is governed by the value of {@link Cache#getStatistics()}.
 *
 * <p>NOTE: computation of highly accurate statistics is expensive.</p>
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
@Component("ehcacheMonitor")
public class EhCacheMonitor extends AbstractCacheMonitor {

    @Nullable
    @Autowired(required=false)
    @Qualifier("ehcacheMonitorCache")
    private Cache cache;

    /**
     * Instantiates a new Ehcache monitor.
     */
    public EhCacheMonitor() {}

    /**
     * Instantiates a new EhCache monitor.
     *
     * @param cache the cache
     */
    public EhCacheMonitor(final Cache cache) {
        this.cache = cache;
    }

    @Override
    protected CacheStatistics[] getStatistics() {
        return new EhCacheStatistics[] {new EhCacheStatistics(cache)};
    }
}
