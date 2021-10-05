package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.CasEventRepositoryFilter;
import org.apereo.cas.support.events.redis.RedisCasEventRepository;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * This is {@link RedisEventsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Configuration(value = "RedisEventsConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RedisEventsConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public PersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisEventConnectionFactory")
    @Autowired
    public RedisConnectionFactory redisEventConnectionFactory(final CasConfigurationProperties casProperties) {
        val redis = casProperties.getEvents().getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "redisEventTemplate")
    @Autowired
    public RedisTemplate redisEventTemplate(
        @Qualifier("redisEventConnectionFactory")
        final RedisConnectionFactory redisEventConnectionFactory) {
        return RedisObjectFactory.newRedisTemplate(redisEventConnectionFactory);
    }


    @ConditionalOnMissingBean(name = "redisEventRepositoryFilter")
    @Bean
    public CasEventRepositoryFilter redisEventRepositoryFilter() {
        return CasEventRepositoryFilter.noOp();
    }

    @Bean
    @Autowired
    public CasEventRepository casEventRepository(
        @Qualifier("redisEventTemplate")
        final RedisTemplate redisEventTemplate,
        @Qualifier("redisEventRepositoryFilter")
        final CasEventRepositoryFilter redisEventRepositoryFilter) {
        return new RedisCasEventRepository(redisEventRepositoryFilter, redisEventTemplate);
    }
}
