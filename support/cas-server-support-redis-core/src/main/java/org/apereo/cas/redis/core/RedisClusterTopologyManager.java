package org.apereo.cas.redis.core;

import module java.base;
import org.apereo.cas.ha.ClusterMember;
import org.apereo.cas.ha.ClusterTopologyManager;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.redis.connection.RedisClusterNode;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * This is {@link RedisClusterTopologyManager}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class RedisClusterTopologyManager implements ClusterTopologyManager {

    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    public List<? extends ClusterMember> discoverMembers() {
        val startedAt = System.nanoTime();
        try (val connection = redisConnectionFactory.getClusterConnection()) {
            val stream = StreamSupport.stream(connection.clusterGetNodes().spliterator(), false);
            return stream.map(node -> {
                val flags = node.getFlags().stream().map(RedisClusterNode.Flag::getRaw).toList();
                return ClusterMember.builder()
                    .owner(getName())
                    .id(node.getId())
                    .address("%s:%s".formatted(node.getHost(), node.getPort()))
                    .status(node.getLinkState() == RedisClusterNode.LinkState.CONNECTED)
                    .responseTime(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt))
                    .attributes(Map.of(
                        "type", node.getType() != null ? node.getType().name() : "UNKNOWN",
                        "flags", flags
                    ))
                    .build();
            })
                .toList();
        }
    }
}
