package org.apereo.cas.redis.core;

import java.util.ArrayList;
import java.util.List;

import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.apereo.cas.configuration.model.support.redis.RedisTicketRegistryProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.util.StringUtils;

import redis.clients.jedis.JedisPoolConfig;

/**
 * This is {@link RedisObjectFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class RedisObjectFactory {

    /**
     * New redis connection factory.
     *
     * @param redis the redis
     * @return the redis connection factory
     */
    public RedisConnectionFactory newRedisConnectionFactory(final BaseRedisProperties redis) {
        final JedisPoolConfig poolConfig = redis.getPool() != null ? jedisPoolConfig(redis) : new JedisPoolConfig();
        final JedisConnectionFactory factory = new JedisConnectionFactory(potentiallyGetSentinelConfig(redis), poolConfig);
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
        factory.setUsePool(redis.isUsePool());

        return factory;
    }

    private JedisPoolConfig jedisPoolConfig(final BaseRedisProperties redis) {
        final JedisPoolConfig config = new JedisPoolConfig();
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
        return config;
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
