package org.apereo.cas.monitor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.spy.memcached.MemcachedClientIF;
import org.apache.commons.pool2.ObjectPool;
import org.springframework.boot.actuate.health.Health;

import java.net.InetSocketAddress;
import java.util.ArrayList;
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
                                    final long evictionThreshold, final long threshold) {
        super(evictionThreshold, threshold);
        this.connectionPool = client;
    }

    @Override
    protected void doHealthCheck(final Health.Builder builder) {
        try {
            val client = getClientFromPool();
            if (client.getAvailableServers().isEmpty()) {
                LOGGER.warn("No available memcached servers can be found");
                builder.outOfService().withDetail("message", "No memcached servers available.");
                return;
            }
            val unavailableList = client.getUnavailableServers();
            if (!unavailableList.isEmpty()) {
                val description = "One or more memcached servers is unavailable: " + unavailableList;
                builder.down().withDetail("message", description);
                return;
            }
            super.doHealthCheck(builder);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            builder.down()
                .withException(e)
                .withDetail("message", "Unable to determine memcached server status.");
        }

    }

    @Override
    protected CacheStatistics[] getStatistics() {
        try {
            val client = getClientFromPool();
            val statsList = new ArrayList<CacheStatistics>(client.getStats().size());
            client.getStats()
                .forEach((key, statsMap) -> {
                    if (!statsMap.isEmpty()) {
                        val size = Long.parseLong(statsMap.get("bytes"));
                        val capacity = Long.parseLong(statsMap.get("limit_maxbytes"));
                        val evictions = Long.parseLong(statsMap.get("evictions"));

                        val name = key instanceof InetSocketAddress
                            ? ((InetSocketAddress) key).getHostName()
                            : key.toString();
                        statsList.add(new SimpleCacheStatistics(size, capacity, evictions, name));
                    }
                });
            return statsList.toArray(CacheStatistics[]::new);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return List.of().toArray(CacheStatistics[]::new);
    }

    private MemcachedClientIF getClientFromPool() throws Exception {
        return this.connectionPool.borrowObject();
    }
}
