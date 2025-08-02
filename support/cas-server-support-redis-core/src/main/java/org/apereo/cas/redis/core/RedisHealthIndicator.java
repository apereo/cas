package org.apereo.cas.redis.core;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisConnectionUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This is {@link RedisHealthIndicator}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class RedisHealthIndicator extends AbstractHealthIndicator {
    private final ApplicationContext applicationContext;

    @Override
    protected void doHealthCheck(final Health.Builder builder) {
        builder.up();
        applicationContext.getBeansOfType(RedisConnectionFactory.class)
            .forEach((key, factory) -> {
                val connection = RedisConnectionUtils.getConnection(factory);
                try {
                    val section = new HashMap<String, Map>();

                    val entries = new HashMap<>();
                    val redisServerCommands = connection.serverCommands();
                    entries.put("server", Objects.requireNonNull(redisServerCommands.info("server")));
                    entries.put("clients", Objects.requireNonNull(redisServerCommands.info("clients")));
                    entries.put("memory", Objects.requireNonNull(redisServerCommands.info("memory")));
                    entries.put("persistence", Objects.requireNonNull(redisServerCommands.info("persistence")));
                    entries.put("stats", Objects.requireNonNull(redisServerCommands.info("stats")));
                    entries.put("replication", Objects.requireNonNull(redisServerCommands.info("replication")));
                    entries.put("cpu", Objects.requireNonNull(redisServerCommands.info("cpu")));
                    entries.put("latencystats", Objects.requireNonNull(redisServerCommands.info("latencystats")));
                    entries.put("cluster", Objects.requireNonNull(redisServerCommands.info("cluster")));
                    entries.put("keyspace", Objects.requireNonNull(redisServerCommands.info("keyspace")));

                    section.put(key, entries);
                    builder.withDetails(section);
                } finally {
                    RedisConnectionUtils.releaseConnection(connection, factory);
                }
            });

        val redisTicketRegistryCache = applicationContext.getBean("redisTicketRegistryCache", Cache.class);

        val section = new HashMap<String, Map>();
        val entries = new HashMap<>();
        entries.put("estimatedSize", redisTicketRegistryCache.estimatedSize());
        entries.put("recodingStats", redisTicketRegistryCache.policy().isRecordingStats());

        val stats = redisTicketRegistryCache.stats();
        entries.put("averageLoadPenalty", stats.averageLoadPenalty());
        entries.put("evictionCount", stats.evictionCount());
        entries.put("evictionWeight", stats.evictionWeight());
        entries.put("hitCount", stats.hitCount());
        entries.put("hitRate", stats.hitRate());
        entries.put("loadCount", stats.loadCount());
        entries.put("loadFailureCount", stats.loadFailureCount());
        entries.put("loadFailureRate", stats.loadFailureRate());
        entries.put("loadSuccessCount", stats.loadSuccessCount());
        entries.put("loadTotalTime", stats.totalLoadTime());
        section.put("redisTicketRegistryCache", entries);
        builder.withDetails(section);
    }
}
