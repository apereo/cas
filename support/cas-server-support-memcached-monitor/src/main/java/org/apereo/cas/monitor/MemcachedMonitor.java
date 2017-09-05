package org.apereo.cas.monitor;

import net.spy.memcached.MemcachedClientIF;
import org.apache.commons.pool2.ObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Monitors the memcached hosts known to an instance of {@link net.spy.memcached.MemcachedClientIF}.
 *
 * @author Marvin S. Addison
 * @since 3.5.1
 */
public class MemcachedMonitor extends AbstractCacheMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MemcachedMonitor.class);

    private final ObjectPool<MemcachedClientIF> connectionPool;

    /**
     * Creates a new monitor that observes the given memcached client.
     *
     * @param client Memcached client.
     */
    public MemcachedMonitor(final ObjectPool<MemcachedClientIF> client) {
        super(MemcachedMonitor.class.getSimpleName());
        this.connectionPool = client;
    }

    /**
     * Supersede the default cache status algorithm by considering unavailable memcached nodes above cache statistics.
     * If all nodes are unavailable, raise an error; if one or more nodes are unavailable, raise a warning; otherwise
     * delegate to examination of cache statistics.
     *
     * @return Cache status descriptor.
     */
    @Override
    public CacheStatus observe() {
        try {
            final MemcachedClientIF client = getClientFromPool();
            if (client.getAvailableServers().isEmpty()) {
                return new CacheStatus(StatusCode.ERROR, "No memcached servers available.");
            }
            final Collection<SocketAddress> unavailableList = client.getUnavailableServers();
            final CacheStatus status;
            if (!unavailableList.isEmpty()) {
                final String description = "One or more memcached servers is unavailable: " + unavailableList;
                status = new CacheStatus(StatusCode.WARN, description, getStatistics());
            } else {
                status = super.observe();
            }
            return status;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new CacheStatus(StatusCode.ERROR, "Unable to determine memcached server status.");
    }


    /**
     * Get cache statistics for all memcached hosts known to {@link MemcachedClientIF}.
     *
     * @return Statistics for all available hosts.
     */
    @Override
    protected CacheStatistics[] getStatistics() {
        final List<CacheStatistics> statsList = new ArrayList<>();
        try {
            final MemcachedClientIF client = getClientFromPool();
            client.getStats()
                    .forEach((key, statsMap) -> {
                        if (!statsMap.isEmpty()) {
                            final long size = Long.parseLong(statsMap.get("bytes"));
                            final long capacity = Long.parseLong(statsMap.get("limit_maxbytes"));
                            final long evictions = Long.parseLong(statsMap.get("evictions"));

                            final String name;
                            if (key instanceof InetSocketAddress) {
                                name = ((InetSocketAddress) key).getHostName();
                            } else {
                                name = key.toString();
                            }
                            statsList.add(new SimpleCacheStatistics(size, capacity, evictions, name));
                        }
                    });
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return statsList.toArray(new CacheStatistics[statsList.size()]);
    }

    private MemcachedClientIF getClientFromPool() throws Exception {
        return this.connectionPool.borrowObject();
    }
}
