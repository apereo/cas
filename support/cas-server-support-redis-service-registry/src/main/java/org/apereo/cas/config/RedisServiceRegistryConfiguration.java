package org.apereo.cas.config;

import org.apereo.cas.adaptors.redis.services.RedisServiceRegistryDao;
import org.apereo.cas.adaptors.redis.services.RegisteredServiceRedisTemplate;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.redis.RedisServiceRegistryProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.services.ServiceRegistryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * This is {@link RedisServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("redisServiceRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RedisServiceRegistryConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @RefreshScope
    public RedisConnectionFactory redisConnectionFactory() {
        final RedisServiceRegistryProperties redis = casProperties.getServiceRegistry().getRedis();
        final RedisObjectFactory obj = new RedisObjectFactory();
        return obj.newRedisConnectionFactory(redis);
    }

    @Bean
    @RefreshScope
    public RedisTemplate registeredServiceRedisTemplate() {
        return new RegisteredServiceRedisTemplate(redisConnectionFactory());
    }

    @Bean
    @RefreshScope
    public ServiceRegistryDao serviceRegistryDao() {
        return new RedisServiceRegistryDao(registeredServiceRedisTemplate());
    }
}
