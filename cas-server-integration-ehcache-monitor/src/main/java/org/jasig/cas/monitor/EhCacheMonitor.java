package org.jasig.cas.monitor;

import net.sf.ehcache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Monitors a {@link Cache} instance.
 * The accuracy of statistics is governed by the value of {@link Cache#getStatistics()}.
 * <p>
 * <p>NOTE: computation of highly accurate statistics is expensive.</p>
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
@Component("ehcacheMonitor")
public class EhCacheMonitor extends AbstractCacheMonitor {

    /**
     * The Ticket granting tickets cache.
     */
    @Nullable
    @Autowired(required = false)
    @Qualifier("ticketGrantingTicketsCache")
    private Cache ticketGrantingTicketsCache;

    /**
     * The Service tickets cache.
     */
    @Nullable
    @Autowired(required = false)
    @Qualifier("serviceTicketsCache")
    private Cache serviceTicketsCache;

    /**
     * Instantiates a new Ehcache monitor.
     */
    public EhCacheMonitor() {
    }


    /**
     * Instantiates a new Eh cache monitor.
     *
     * @param ticketGrantingTicketsCache the ticket granting tickets cache
     */
    public EhCacheMonitor(final Cache ticketGrantingTicketsCache) {
        this.ticketGrantingTicketsCache = ticketGrantingTicketsCache;
    }

    /**
     * Instantiates a new Eh cache monitor.
     *
     * @param ticketGrantingTicketsCache the ticket granting tickets cache
     * @param serviceTicketsCache        the service tickets cache
     */
    public EhCacheMonitor(final Cache ticketGrantingTicketsCache, final Cache serviceTicketsCache) {
        this.ticketGrantingTicketsCache = ticketGrantingTicketsCache;
        this.serviceTicketsCache = serviceTicketsCache;
    }
    
    @Override
    protected CacheStatistics[] getStatistics() {
        final List<CacheStatistics> list = new ArrayList<>();
        if (this.ticketGrantingTicketsCache != null) {
            list.add(new EhCacheStatistics(this.ticketGrantingTicketsCache));
        }
        if (this.serviceTicketsCache != null) {
            list.add(new EhCacheStatistics(this.serviceTicketsCache));
        }
        return list.toArray(new CacheStatistics[] {});
    }
}
