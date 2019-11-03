package org.apereo.cas.config;

import org.apereo.cas.adaptors.redis.services.RedisServiceRegistry;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;

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

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("serviceRegistryListeners")
    private ObjectProvider<Collection<ServiceRegistryListener>> serviceRegistryListeners;
    
    @Bean
    @ConditionalOnMissingBean(name = "redisServiceConnectionFactory")
    public RedisConnectionFactory redisServiceConnectionFactory() {
        val redis = casProperties.getServiceRegistry().getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis);
    }

    @Bean
    @ConditionalOnMissingBean(name = "registeredServiceRedisTemplate")
    public RedisTemplate registeredServiceRedisTemplate() {
        return RedisObjectFactory.newRedisTemplate(redisServiceConnectionFactory());
    }

    @Bean
    @RefreshScope
    public ServiceRegistry redisServiceRegistry() {
        return new RedisServiceRegistry(applicationContext, registeredServiceRedisTemplate(), serviceRegistryListeners.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisServiceRegistryExecutionPlanConfigurer")
    public ServiceRegistryExecutionPlanConfigurer redisServiceRegistryExecutionPlanConfigurer() {
        return plan -> plan.registerServiceRegistry(redisServiceRegistry());
    }

}
