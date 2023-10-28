package org.apereo.cas.redis.core;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.apereo.cas.configuration.model.support.redis.RedisClusterNodeProperties;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import com.redis.lettucemod.search.Field;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.RedisConnectionException;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RedisObjectFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Redis")
@EnabledIfListeningOnPort(port = 6379)
class RedisObjectFactoryTests {
    @Test
    void verifyRedisSearchCommandSupported() throws Throwable {
        val props = new BaseRedisProperties();
        props.setHost("localhost");
        props.setPort(6379);
        val command = RedisObjectFactory.newRedisModulesCommands(props);
        assertFalse(command.isEmpty());
        val indexName = UUID.randomUUID().toString();
        val result = command.get().ftCreate(indexName,
            Field.text("name").build(),
            Field.numeric("id").build());
        assertEquals("OK", result);
        val info = command.get().ftInfo(indexName);
        assertNotNull(info);
    }


    @Test
    void verifyConnection() throws Throwable {
        val props = new BaseRedisProperties();
        props.setHost("localhost");
        props.setPort(6379);
        props.getPool().setMinEvictableIdleTimeMillis(2000);
        props.getPool().setNumTestsPerEvictionRun(1);
        props.getPool().setSoftMinEvictableIdleTimeMillis(1);
        props.getPool().setEnabled(true);
        val connection = RedisObjectFactory.newRedisConnectionFactory(props, true, CasSSLContext.disabled());
        assertNotNull(connection);
    }

    @Test
    void verifyConnectionWithUsernamePassword() throws Throwable {
        val props = new BaseRedisProperties();
        props.setHost("localhost");
        props.setPort(16389);
        props.setUsername("default");
        props.setPassword("pAssw0rd123");
        val connection = RedisObjectFactory.newRedisModulesCommands(props);
        assertNotNull(connection);
    }

    @Test
    void verifyConnectionWithPassword() throws Throwable {
        val props = new BaseRedisProperties();
        props.setHost("localhost");
        props.setPort(16389);
        props.setUsername(null);
        props.setPassword("pAssw0rd123");
        assertThrows(RedisConnectionException.class, () -> RedisObjectFactory.newRedisModulesCommands(props));
    }

    @Test
    void verifyConnectionURI() throws Throwable {
        val props = new BaseRedisProperties();
        props.setUri("redis://localhost:6379");
        val connection = RedisObjectFactory.newRedisConnectionFactory(props, true, CasSSLContext.disabled());
        assertNotNull(connection);
    }

    @Test
    void verifyClusterConnection() throws Throwable {
        val props = new BaseRedisProperties();
        props.getCluster().getNodes().add(new RedisClusterNodeProperties()
            .setType("master")
            .setPort(6379)
            .setId(UUID.randomUUID().toString())
            .setName("redis-master")
            .setHost("localhost"));

        props.getCluster().getNodes().add(new RedisClusterNodeProperties()
            .setType("REPLICA")
            .setPort(6380)
            .setHost("localhost")
            .setId(UUID.randomUUID().toString())
            .setName("redis-slave1")
            .setReplicaOf("redis_server_master"));

        props.getCluster().getNodes().add(new RedisClusterNodeProperties()
            .setType("REPLICA")
            .setPort(6381)
            .setHost("localhost")
            .setId(UUID.randomUUID().toString())
            .setName("redis-slave2")
            .setReplicaOf("redis_server_master"));

        props.getCluster().setMaxRedirects(3);
        props.getCluster().setTopologyRefreshPeriod("PT5S");
        val connection = RedisObjectFactory.newRedisConnectionFactory(props, true, CasSSLContext.disabled());
        assertNotNull(connection);
    }

    @Test
    void verifyNonDefaultClientConnectionOptions() throws Throwable {
        val props = new BaseRedisProperties();
        props.getCluster().getNodes().add(new RedisClusterNodeProperties()
            .setType("master")
            .setPort(6379)
            .setId(UUID.randomUUID().toString())
            .setName("redis-master")
            .setHost("localhost"));

        props.getCluster().getNodes().add(new RedisClusterNodeProperties()
            .setType("REPLICA")
            .setPort(6380)
            .setHost("localhost")
            .setId(UUID.randomUUID().toString())
            .setName("redis-slave1")
            .setReplicaOf("redis_server_master"));

        props.getCluster().getNodes().add(new RedisClusterNodeProperties()
            .setType("REPLICA")
            .setPort(6381)
            .setHost("localhost")
            .setId(UUID.randomUUID().toString())
            .setName("redis-slave2")
            .setReplicaOf("redis_server_master"));

        props.setTimeout(StringUtils.EMPTY);
        props.setConnectTimeout(StringUtils.EMPTY);
        props.getCluster().setAdaptiveTopologyRefresh(true);
        props.getCluster().setDynamicRefreshSources(true);
        props.getCluster().setMaxRedirects(3);
        val connection = RedisObjectFactory.newRedisConnectionFactory(props, true, CasSSLContext.disabled());
        assertNotNull(connection);
    }


    @Test
    void validateRedisReadFromValues() {
        Stream.of(BaseRedisProperties.RedisReadFromTypes.values()).map(Enum::name).forEach(ReadFrom::valueOf);
    }
}
