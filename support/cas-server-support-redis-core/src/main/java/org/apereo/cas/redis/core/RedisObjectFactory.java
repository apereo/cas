package org.apereo.cas.redis.core;

import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;

import io.lettuce.core.ReadFrom;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link RedisObjectFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class RedisObjectFactory {

    /**
     * New redis template.
     *
     * @param <K>               the type parameter
     * @param <V>               the type parameter
     * @param connectionFactory the connection factory
     * @return the redis template
     */
    public static <K, V> RedisTemplate<K, V> newRedisTemplate(final RedisConnectionFactory connectionFactory) {
        val template = new RedisTemplate<K, V>();
        val string = new StringRedisSerializer();
        val jdk = new JdkSerializationRedisSerializer();
        template.setKeySerializer(string);
        template.setValueSerializer(jdk);
        template.setHashValueSerializer(jdk);
        template.setHashKeySerializer(string);
        template.setConnectionFactory(connectionFactory);
        return template;
    }

    /**
     * New redis connection factory.
     *
     * @param redis the redis
     * @return the redis connection factory
     */
    public static RedisConnectionFactory newRedisConnectionFactory(final BaseRedisProperties redis) {
        return newRedisConnectionFactory(redis, false);
    }

    /**
     * New redis connection factory.
     *
     * @param redis      the redis
     * @param initialize the initialize
     * @return the redis connection factory
     */
    public static RedisConnectionFactory newRedisConnectionFactory(final BaseRedisProperties redis,
                                                                   final boolean initialize) {
        var factory = (LettuceConnectionFactory) null;
        if (redis.getSentinel() != null && StringUtils.hasText(redis.getSentinel().getMaster())) {
            factory = new LettuceConnectionFactory(getSentinelConfig(redis), getRedisPoolConfig(redis));
        } else if (redis.getCluster() != null && !redis.getCluster().getNodes().isEmpty()) {
            factory = new LettuceConnectionFactory(getClusterConfig(redis), getRedisPoolConfig(redis));
        } else {
            factory = new LettuceConnectionFactory(getStandaloneConfig(redis), getRedisPoolConfig(redis));
        }

        if (initialize) {
            factory.afterPropertiesSet();
        }
        return factory;
    }

    private static RedisClusterConfiguration getClusterConfig(final BaseRedisProperties redis) {
        val redisConfiguration = new RedisClusterConfiguration();
        val cluster = redis.getCluster();

        cluster.getNodes()
            .stream()
            .filter(nodeConfig -> StringUtils.hasText(nodeConfig.getHost())
                && nodeConfig.getPort() > 0
                && StringUtils.hasText(nodeConfig.getType()))
            .forEach(nodeConfig -> {
                LOGGER.trace("Building redis cluster node for [{}]", nodeConfig);
                
                val nodeBuilder = new RedisNode.RedisNodeBuilder()
                    .listeningAt(nodeConfig.getHost(), nodeConfig.getPort())
                    .promotedAs(RedisNode.NodeType.valueOf(nodeConfig.getType().toUpperCase()));

                if (StringUtils.hasText(nodeConfig.getReplicaOf())) {
                    nodeBuilder.replicaOf(nodeConfig.getReplicaOf());
                }
                if (StringUtils.hasText(nodeConfig.getId())) {
                    nodeBuilder.withId(nodeConfig.getId());
                }
                if (StringUtils.hasText(nodeConfig.getName())) {
                    nodeBuilder.withName(nodeConfig.getName());
                }
                redisConfiguration.clusterNode(nodeBuilder.build());
            });
        if (StringUtils.hasText(cluster.getPassword())) {
            redisConfiguration.setPassword(cluster.getPassword());
        }
        if (cluster.getMaxRedirects() > 0) {
            redisConfiguration.setMaxRedirects(cluster.getMaxRedirects());
        }
        return redisConfiguration;
    }

    private static LettucePoolingClientConfiguration getRedisPoolConfig(final BaseRedisProperties redis) {
        val poolConfig = LettucePoolingClientConfiguration.builder();
        if (redis.isUseSsl()) {
            poolConfig.useSsl();
            LOGGER.trace("Redis configuration: SSL connections are enabled");
        }
        if (StringUtils.hasText(redis.getReadFrom())) {
            poolConfig.readFrom(ReadFrom.valueOf(redis.getReadFrom()));
            LOGGER.debug("Redis configuration: readFrom property is set to [{}]", redis.getReadFrom());
        }
        if (redis.getTimeout() > 0) {
            poolConfig.commandTimeout(Duration.ofMillis(redis.getTimeout()));
            LOGGER.trace("Redis configuration: commandTimeout is set to [{}]ms", redis.getTimeout());
        }

        val pool = redis.getPool();
        if (pool != null && pool.isEnabled()) {
            val config = new GenericObjectPoolConfig();
            config.setMaxTotal(pool.getMaxActive());
            config.setMaxIdle(pool.getMaxIdle());
            config.setMinIdle(pool.getMinIdle());
            config.setMaxWaitMillis(pool.getMaxWait());
            config.setLifo(pool.isLifo());
            config.setFairness(pool.isFairness());
            config.setTestWhileIdle(pool.isTestWhileIdle());
            config.setTestOnBorrow(pool.isTestOnBorrow());
            config.setTestOnReturn(pool.isTestOnReturn());
            config.setTestOnCreate(pool.isTestOnCreate());
            if (pool.getMinEvictableIdleTimeMillis() > 0) {
                config.setMinEvictableIdleTimeMillis(pool.getMinEvictableIdleTimeMillis());
            }
            if (pool.getNumTestsPerEvictionRun() > 0) {
                config.setNumTestsPerEvictionRun(pool.getNumTestsPerEvictionRun());
            }
            if (pool.getSoftMinEvictableIdleTimeMillis() > 0) {
                config.setSoftMinEvictableIdleTimeMillis(pool.getSoftMinEvictableIdleTimeMillis());
            }
            poolConfig.poolConfig(config);
            LOGGER.trace("Redis configuration: the pool is configured to [{}]", config);
        }
        return poolConfig.build();
    }

    private static RedisConfiguration getStandaloneConfig(final BaseRedisProperties redis) {

        LOGGER.debug("Setting Redis standalone configuration on host [{}] and port [{}]", redis.getHost(), redis.getPort());
        val standaloneConfig = new RedisStandaloneConfiguration(redis.getHost(), redis.getPort());
        standaloneConfig.setDatabase(redis.getDatabase());
        if (StringUtils.hasText(redis.getPassword())) {
            standaloneConfig.setPassword(RedisPassword.of(redis.getPassword()));
        }
        return standaloneConfig;
    }

    private static RedisSentinelConfiguration getSentinelConfig(final BaseRedisProperties redis) {
        LOGGER.debug("Setting Redis with Sentinel configuration on master [{}]", redis.getSentinel().getMaster());
        val sentinelConfig = new RedisSentinelConfiguration()
            .master(redis.getSentinel().getMaster());
        LOGGER.debug("Sentinel nodes configured are [{}]", redis.getSentinel().getNode());
        sentinelConfig.setSentinels(createRedisNodesForProperties(redis));
        sentinelConfig.setDatabase(redis.getDatabase());
        if (StringUtils.hasText(redis.getPassword())) {
            sentinelConfig.setPassword(RedisPassword.of(redis.getPassword()));
        }
        return sentinelConfig;
    }

    private static List<RedisNode> createRedisNodesForProperties(final BaseRedisProperties redis) {
        if (redis.getSentinel().getNode() != null) {
            val nodes = redis.getSentinel().getNode();
            return nodes
                .stream()
                .map(hostAndPort -> StringUtils.split(hostAndPort, ":"))
                .filter(Objects::nonNull)
                .map(args -> new RedisNode(args[0], Integer.parseInt(args[1])))
                .collect(Collectors.toCollection(ArrayList::new));
        }
        return new ArrayList<>(0);
    }
}
