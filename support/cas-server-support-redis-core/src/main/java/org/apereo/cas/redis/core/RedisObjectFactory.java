package org.apereo.cas.redis.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

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
     * @param keyClass          the key class
     * @param valueClass        the value class
     * @return the redis template
     */
    public <K, V> RedisTemplate<K, V> newRedisTemplate(final RedisConnectionFactory connectionFactory,
                                                       final Class<K> keyClass, final Class<V> valueClass) {
        final RedisTemplate<K, V> template = new RedisTemplate();
        final RedisSerializer<String> string = new StringRedisSerializer();
        final var jdk = new JdkSerializationRedisSerializer();
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
    public RedisConnectionFactory newRedisConnectionFactory(final BaseRedisProperties redis) {
        final var poolConfig = redis.getPool() != null
            ? redisPoolConfig(redis)
            : LettucePoolingClientConfiguration.defaultConfiguration();

        final var factory = new LettuceConnectionFactory(potentiallyGetSentinelConfig(redis), poolConfig);
        factory.setHostName(redis.getHost());
        factory.setPort(redis.getPort());
        if (redis.getPassword() != null) {
            factory.setPassword(redis.getPassword());
        }
        factory.setDatabase(redis.getDatabase());
        if (redis.getTimeout() > 0) {
            factory.setTimeout(redis.getTimeout());
        }
        factory.setUseSsl(redis.isUseSsl());

        return factory;
    }

    private LettucePoolingClientConfiguration redisPoolConfig(final BaseRedisProperties redis) {
        final var config = new GenericObjectPoolConfig();
        final var props = redis.getPool();
        config.setMaxTotal(props.getMaxActive());
        config.setMaxIdle(props.getMaxIdle());
        config.setMinIdle(props.getMinIdle());
        config.setMaxWaitMillis(props.getMaxWait());
        config.setLifo(props.isLifo());
        config.setFairness(props.isFairness());
        config.setTestWhileIdle(props.isTestWhileIdle());
        config.setTestOnBorrow(props.isTestOnBorrow());
        config.setTestOnReturn(props.isTestOnReturn());
        config.setTestOnCreate(props.isTestOnCreate());

        if (props.getMinEvictableIdleTimeMillis() > 0) {
            config.setMinEvictableIdleTimeMillis(props.getMinEvictableIdleTimeMillis());
        }
        if (props.getNumTestsPerEvictionRun() > 0) {
            config.setNumTestsPerEvictionRun(props.getNumTestsPerEvictionRun());
        }
        if (props.getSoftMinEvictableIdleTimeMillis() > 0) {
            config.setSoftMinEvictableIdleTimeMillis(props.getSoftMinEvictableIdleTimeMillis());
        }
        return LettucePoolingClientConfiguration.builder()
            .poolConfig(config)
            .build();
    }

    private RedisSentinelConfiguration potentiallyGetSentinelConfig(final BaseRedisProperties redis) {
        if (redis.getSentinel() == null) {
            return null;
        }
        RedisSentinelConfiguration sentinelConfig = null;
        if (redis.getSentinel() != null) {
            sentinelConfig = new RedisSentinelConfiguration().master(redis.getSentinel().getMaster());
            sentinelConfig.setSentinels(createRedisNodesForProperties(redis));
        }
        return sentinelConfig;
    }

    private List<RedisNode> createRedisNodesForProperties(final BaseRedisProperties redis) {
        final List<RedisNode> redisNodes = new ArrayList<>();
        if (redis.getSentinel().getNode() != null) {
            final var nodes = redis.getSentinel().getNode();
            for (final var hostAndPort: nodes) {
                final var args = StringUtils.split(hostAndPort, ":");
                redisNodes.add(new RedisNode(args[0], Integer.parseInt(args[1])));
            }
        }
        return redisNodes;
    }
}
