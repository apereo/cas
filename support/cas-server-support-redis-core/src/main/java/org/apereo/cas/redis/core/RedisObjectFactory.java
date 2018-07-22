package org.apereo.cas.redis.core;

import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;

import lombok.val;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
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

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link RedisObjectFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
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
    public RedisConnectionFactory newRedisConnectionFactory(final BaseRedisProperties redis) {
        val poolConfig = redis.getPool() != null
            ? redisPoolConfig(redis)
            : LettucePoolingClientConfiguration.defaultConfiguration();

        val sentinelConfiguration = redis.getSentinel() == null
            ? null
            : potentiallyGetSentinelConfig(redis);

        val standaloneConfig = new RedisStandaloneConfiguration(redis.getHost(), redis.getPort());
        standaloneConfig.setDatabase(redis.getDatabase());
        if (StringUtils.hasText(redis.getPassword())) {
            standaloneConfig.setPassword(RedisPassword.of(redis.getPassword()));
        }

        val factory = sentinelConfiguration != null
            ? new LettuceConnectionFactory(sentinelConfiguration, poolConfig)
            : new LettuceConnectionFactory(standaloneConfig);

        factory.setHostName(redis.getHost());
        factory.setPort(redis.getPort());
        if (StringUtils.hasText(redis.getPassword())) {
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
        val config = new GenericObjectPoolConfig();
        val props = redis.getPool();
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
        val sentinelConfig = new RedisSentinelConfiguration().master(redis.getSentinel().getMaster());
        sentinelConfig.setSentinels(createRedisNodesForProperties(redis));

        return sentinelConfig;
    }

    private List<RedisNode> createRedisNodesForProperties(final BaseRedisProperties redis) {
        val redisNodes = new ArrayList<RedisNode>();
        if (redis.getSentinel().getNode() != null) {
            val nodes = redis.getSentinel().getNode();
            for (val hostAndPort : nodes) {
                val args = StringUtils.split(hostAndPort, ":");
                redisNodes.add(new RedisNode(args[0], Integer.parseInt(args[1])));
            }
        }
        return redisNodes;
    }
}
