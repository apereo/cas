package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.CasEventRepositoryFilter;
import org.apereo.cas.support.events.redis.RedisCasEventRepository;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * This is {@link RedisEventsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Configuration("RedisEventsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RedisEventsConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public PersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisEventConnectionFactory")
    public RedisConnectionFactory redisEventConnectionFactory() {
        val redis = casProperties.getEvents().getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis);
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "redisEventTemplate")
    public RedisTemplate redisEventTemplate() {
        return RedisObjectFactory.newRedisTemplate(redisEventConnectionFactory());
    }


    @ConditionalOnMissingBean(name = "redisEventRepositoryFilter")
    @Bean
    public CasEventRepositoryFilter redisEventRepositoryFilter() {
        return CasEventRepositoryFilter.noOp();
    }

    @Bean
    public CasEventRepository casEventRepository() {
        return new RedisCasEventRepository(redisEventRepositoryFilter(), redisEventTemplate());
    }
}
