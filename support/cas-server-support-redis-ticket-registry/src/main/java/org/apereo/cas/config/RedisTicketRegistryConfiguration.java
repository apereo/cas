package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.redis.RedisTicketRegistryProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.registry.RedisTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRedisTemplate;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPoolConfig;

/**
 * This is {@link RedisTicketRegistryConfiguration}.
 *
 * @author serv
 * @since 5.0.0
 */
@Configuration("redisTicketRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RedisTicketRegistryConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Bean
    @RefreshScope
    public RedisConnectionFactory redisConnectionFactory() {
        final RedisTicketRegistryProperties redis = casProperties.getTicket().getRegistry().getRedis();
        final JedisPoolConfig poolConfig = redis.getPool() != null ? jedisPoolConfig() : new JedisPoolConfig();

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
        return factory;
    }

    private JedisPoolConfig jedisPoolConfig() {
        final RedisTicketRegistryProperties redis = casProperties.getTicket().getRegistry().getRedis();

        final JedisPoolConfig config = new JedisPoolConfig();
        final RedisTicketRegistryProperties.Pool props = redis.getPool();
        config.setMaxTotal(props.getMaxActive());
        config.setMaxIdle(props.getMaxIdle());
        config.setMinIdle(props.getMinIdle());
        config.setMaxWaitMillis(props.getMaxWait());
        return config;
    }


    @Bean
    @RefreshScope
    public TicketRedisTemplate ticketRedisTemplate() {
        return new TicketRedisTemplate(redisConnectionFactory());
    }

    @Bean
    @RefreshScope
    public TicketRegistry ticketRegistry() {
        final RedisTicketRegistryProperties redis = casProperties.getTicket().getRegistry().getRedis();
        final RedisTicketRegistry r = new RedisTicketRegistry(ticketRedisTemplate());
        r.setCipherExecutor(Beans.newTicketRegistryCipherExecutor(redis.getCrypto()));
        return r;
    }
}
