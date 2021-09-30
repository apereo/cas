package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.web.support.RedisThrottledSubmissionHandlerInterceptorAdapter;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerConfigurationContext;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * This is {@link CasRedisThrottlingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.audit.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
@Configuration(value = "casRedisThrottlingConfiguration", proxyBeanMethods = false)
public class CasRedisThrottlingConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "redisThrottleConnectionFactory")
    @Autowired
    public RedisConnectionFactory redisThrottleConnectionFactory(final CasConfigurationProperties casProperties) {
        val redis = casProperties.getAudit().getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis);
    }

    @Bean
    @ConditionalOnMissingBean(name = "throttleRedisTemplate")
    public RedisTemplate throttleRedisTemplate(
        @Qualifier("redisThrottleConnectionFactory")
        final RedisConnectionFactory redisThrottleConnectionFactory) {
        return RedisObjectFactory.newRedisTemplate(redisThrottleConnectionFactory);
    }

    @Autowired
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ThrottledSubmissionHandlerInterceptor authenticationThrottle(
        @Qualifier("throttleRedisTemplate")
        final RedisTemplate throttleRedisTemplate,
        @Qualifier("authenticationThrottlingConfigurationContext")
        final ThrottledSubmissionHandlerConfigurationContext authenticationThrottlingConfigurationContext) {
        return new RedisThrottledSubmissionHandlerInterceptorAdapter(authenticationThrottlingConfigurationContext, throttleRedisTemplate);
    }
}
