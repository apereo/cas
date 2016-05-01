package org.jasig.cas.monitor;

import net.sf.ehcache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

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
@RefreshScope
@Component("ehcacheMonitor")
public class EhCacheMonitor extends AbstractCacheMonitor {

    /**
     * The Ticket granting tickets cache.
     */
    @Autowired(required = false)
    @Qualifier("ehcacheTicketsCache")
    private Cache ehcacheTicketsCache;

    /**
     * Instantiates a new Ehcache monitor.
     */
    public EhCacheMonitor() {
    }

    /**
     * Instantiates a new Eh cache monitor.
     *
     * @param ehcacheTicketsCache the tickets cache
     */
    public EhCacheMonitor(final Cache ehcacheTicketsCache) {
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
