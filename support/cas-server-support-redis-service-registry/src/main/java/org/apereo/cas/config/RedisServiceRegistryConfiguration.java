package org.apereo.cas.config;

import org.apereo.cas.adaptors.redis.services.RedisServiceRegistry;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link RedisServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "redisServiceRegistryConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.service-registry.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RedisServiceRegistryConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "redisServiceConnectionFactory")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public RedisConnectionFactory redisServiceConnectionFactory(final CasConfigurationProperties casProperties) {
        val redis = casProperties.getServiceRegistry().getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis);
    }

    @Bean
    @Autowired
    @ConditionalOnMissingBean(name = "registeredServiceRedisTemplate")
    public RedisTemplate<String, RegisteredService> registeredServiceRedisTemplate(
        @Qualifier("redisServiceConnectionFactory")
        final RedisConnectionFactory redisServiceConnectionFactory) {
        return RedisObjectFactory.newRedisTemplate(redisServiceConnectionFactory);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    @ConditionalOnMissingBean(name = "redisServiceRegistry")
    public ServiceRegistry redisServiceRegistry(
        final ObjectProvider<List<ServiceRegistryListener>> serviceRegistryListeners,
        @Qualifier("registeredServiceRedisTemplate")
        final RedisTemplate<String, RegisteredService> registeredServiceRedisTemplate, final ConfigurableApplicationContext applicationContext) {
        return new RedisServiceRegistry(applicationContext, registeredServiceRedisTemplate,
            Optional.ofNullable(serviceRegistryListeners.getIfAvailable()).orElseGet(ArrayList::new));
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisServiceRegistryExecutionPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public ServiceRegistryExecutionPlanConfigurer redisServiceRegistryExecutionPlanConfigurer(
        @Qualifier("redisServiceRegistry")
        final ServiceRegistry redisServiceRegistry) {
        return plan -> plan.registerServiceRegistry(redisServiceRegistry);
    }
}
