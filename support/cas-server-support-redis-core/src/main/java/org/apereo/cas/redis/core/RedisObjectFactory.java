package org.apereo.cas.redis.core;

import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.apereo.cas.configuration.model.support.redis.RedisTicketRegistryProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
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
        final JedisConnectionFactory factory = new JedisConnectionFactory(poolConfig);
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
        factory.setTimeout(redis.getTimeout());
        factory.setUseSsl(redis.isUseSsl());
        
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
}
