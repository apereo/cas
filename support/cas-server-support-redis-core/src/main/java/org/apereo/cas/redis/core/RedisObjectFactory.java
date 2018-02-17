package org.apereo.cas.redis.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.apereo.cas.configuration.model.support.redis.RedisTicketRegistryProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
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
     * New redis connection factory.
     *
     * @param redis the redis
     * @return the redis connection factory
     */
    public RedisConnectionFactory newRedisConnectionFactory(final BaseRedisProperties redis) {
        final LettucePoolingClientConfiguration poolConfig = redis.getPool() != null
            ? redisPoolConfig(redis)
            : LettucePoolingClientConfiguration.defaultConfiguration();

        final LettuceConnectionFactory factory = new LettuceConnectionFactory(potentiallyGetSentinelConfig(redis), poolConfig);
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
        final GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        final RedisTicketRegistryProperties.Pool props = redis.getPool();
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
        final List<RedisNode> redisNodes = new ArrayList<RedisNode>();
        if (redis.getSentinel().getNode() != null) {
            final List<String> nodes = redis.getSentinel().getNode();
            for (final String hostAndPort : nodes) {
                final String[] args = StringUtils.split(hostAndPort, ":");
                redisNodes.add(new RedisNode(args[0], Integer.parseInt(args[1])));
            }
        }
        return redisNodes;
    }
}
