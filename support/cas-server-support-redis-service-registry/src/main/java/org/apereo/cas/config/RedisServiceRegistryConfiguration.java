package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.redis.services.RedisServiceRegistry;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlan;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
@Slf4j
public class RedisServiceRegistryConfiguration implements ServiceRegistryExecutionPlanConfigurer {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @ConditionalOnMissingBean(name = "redisServiceConnectionFactory")
    public RedisConnectionFactory redisServiceConnectionFactory() {
        final var redis = casProperties.getServiceRegistry().getRedis();
        final var obj = new RedisObjectFactory();
        return obj.newRedisConnectionFactory(redis);
    }

    @Bean
    @ConditionalOnMissingBean(name = "registeredServiceRedisTemplate")
    public RedisTemplate registeredServiceRedisTemplate() {
        final var obj = new RedisObjectFactory();
        return obj.newRedisTemplate(redisServiceConnectionFactory(), String.class, RegisteredService.class);
    }

    @Bean
    @RefreshScope
    public ServiceRegistry redisServiceRegistry() {
        return new RedisServiceRegistry(registeredServiceRedisTemplate());
    }

    @Override
    public void configureServiceRegistry(final ServiceRegistryExecutionPlan plan) {
        plan.registerServiceRegistry(redisServiceRegistry());
    }
}
