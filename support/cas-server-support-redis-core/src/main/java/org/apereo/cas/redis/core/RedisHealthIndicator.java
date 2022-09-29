package org.apereo.cas.redis.core;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisConnectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

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
                    entries.put("server", Objects.requireNonNull(connection.info("server")));
                    entries.put("clients", Objects.requireNonNull(connection.info("clients")));
                    entries.put("memory", Objects.requireNonNull(connection.info("memory")));
                    entries.put("persistence", Objects.requireNonNull(connection.info("persistence")));
                    entries.put("stats", Objects.requireNonNull(connection.info("stats")));
                    entries.put("replication", Objects.requireNonNull(connection.info("replication")));
                    entries.put("cpu", Objects.requireNonNull(connection.info("cpu")));
                    entries.put("latencystats", Objects.requireNonNull(connection.info("latencystats")));
                    entries.put("cluster", Objects.requireNonNull(connection.info("cluster")));
                    entries.put("keyspace", Objects.requireNonNull(connection.info("keyspace")));
                    
                    section.put(key, entries);
                    builder.withDetails(section);
                } finally {
                    RedisConnectionUtils.releaseConnection(connection, factory);
                }
            });
    }

    protected Map addDetailsFor(final RedisConnection connection, final String section) {
        val entries = new TreeMap();
        entries.put(section, Objects.requireNonNull(connection.info(section)));
        return entries;
    }
}
