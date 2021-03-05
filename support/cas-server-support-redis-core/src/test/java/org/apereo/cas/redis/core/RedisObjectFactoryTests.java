package org.apereo.cas.redis.core;

import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.apereo.cas.configuration.model.support.redis.RedisClusterNodeProperties;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RedisObjectFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Redis")
@Getter
@EnabledIfPortOpen(port = 6379)
public class RedisObjectFactoryTests {
    @Test
    public void verifyConnection() {
        val props = new BaseRedisProperties();
        props.setHost("localhost");
        props.setPort(6379);
        val connection = RedisObjectFactory.newRedisConnectionFactory(props, true);
        assertNotNull(connection);
    }

    @Test
    public void verifyClusterConnection() {
        val props = new BaseRedisProperties();
        props.getCluster().getNodes().add(new RedisClusterNodeProperties()
            .setType("master")
            .setPort(6379)
            .setId(UUID.randomUUID().toString())
            .setName("redis-master")
            .setHost("localhost"));

        props.getCluster().getNodes().add(new RedisClusterNodeProperties()
            .setType("slave")
            .setPort(6380)
            .setHost("localhost")
            .setId(UUID.randomUUID().toString())
            .setName("redis-slave1")
            .setReplicaOf("redis_server_master"));

        props.getCluster().getNodes().add(new RedisClusterNodeProperties()
            .setType("slave")
            .setPort(6381)
            .setHost("localhost")
            .setId(UUID.randomUUID().toString())
            .setName("redis-slave2")
            .setReplicaOf("redis_server_master"));

        props.getCluster().setMaxRedirects(3);
        val connection = RedisObjectFactory.newRedisConnectionFactory(props, true);
        assertNotNull(connection);
    }
}
