package org.apereo.cas.monitor;

import lombok.extern.slf4j.Slf4j;
import net.spy.memcached.MemcachedClientIF;
import org.apache.commons.pool2.ObjectPool;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.boot.actuate.health.Health;

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
@Slf4j
public class MemcachedHealthIndicator extends AbstractCacheHealthIndicator {


    private final ObjectPool<MemcachedClientIF> connectionPool;

    public MemcachedHealthIndicator(final ObjectPool<MemcachedClientIF> client,
                                    final CasConfigurationProperties casProperties) {
        super(casProperties);
        this.connectionPool = client;
    }

    @Override
    protected void doHealthCheck(final Health.Builder builder) throws Exception {
        try {
            final MemcachedClientIF client = getClientFromPool();
            if (client.getAvailableServers().isEmpty()) {
                builder.outOfService().withDetail("message", "No memcached servers available.");
                return;
            }
            final Collection<SocketAddress> unavailableList = client.getUnavailableServers();
            if (!unavailableList.isEmpty()) {
                final String description = "One or more memcached servers is unavailable: " + unavailableList;
                builder.down().withDetail("message", description);
                return;
            }
            super.doHealthCheck(builder);
            return;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            builder.down()
                .withException(e)
                .withDetail("message", "Unable to determine memcached server status.");
        }

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
