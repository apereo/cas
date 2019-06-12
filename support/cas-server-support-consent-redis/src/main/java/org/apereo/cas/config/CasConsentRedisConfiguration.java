package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.consent.RedisConsentRepository;
import org.apereo.cas.redis.core.RedisObjectFactory;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * This is {@link CasConsentRedisConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casConsentRedisConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasConsentRedisConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public ConsentRepository consentRepository() {
        return new RedisConsentRepository(consentRedisTemplate());
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisConsentConnectionFactory")
    public RedisConnectionFactory redisConsentConnectionFactory() {
        val redis = casProperties.getConsent().getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis);
    }

    @Bean
    @ConditionalOnMissingBean(name = "consentRedisTemplate")
    public RedisTemplate consentRedisTemplate() {
        return RedisObjectFactory.newRedisTemplate(redisConsentConnectionFactory());
    }
}
