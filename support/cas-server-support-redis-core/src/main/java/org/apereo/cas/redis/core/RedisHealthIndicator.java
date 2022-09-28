package org.apereo.cas.redis.core;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisConnectionUtils;

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
        applicationContext.getBeansOfType(RedisConnectionFactory.class)
            .forEach((key, factory) -> {
                val connection = RedisConnectionUtils.getConnection(factory);
                try {
                    addDetailsFor(connection, "server", builder);
                    addDetailsFor(connection, "clients", builder);
                    addDetailsFor(connection, "memory", builder);
                    addDetailsFor(connection, "persistence", builder);
                    addDetailsFor(connection, "stats", builder);
                    addDetailsFor(connection, "replication", builder);
                    addDetailsFor(connection, "cpu", builder);
                    addDetailsFor(connection, "latencystats", builder);
                    addDetailsFor(connection, "cluster", builder);
                    addDetailsFor(connection, "keyspace", builder);
                    builder.up();
                } finally {
                    RedisConnectionUtils.releaseConnection(connection, factory);
                }
            });
    }

    protected void addDetailsFor(final RedisConnection connection,
                                 final String section,
                                 final Health.Builder builder) {
        val serverInfo = new TreeMap(Objects.requireNonNull(connection.info(section)));
        builder.withDetails(Map.of(section, serverInfo));
    }
}
