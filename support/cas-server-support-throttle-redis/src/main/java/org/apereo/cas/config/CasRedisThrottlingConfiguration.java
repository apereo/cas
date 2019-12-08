package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.throttle.ThrottledRequestExecutor;
import org.apereo.cas.throttle.ThrottledRequestResponseHandler;
import org.apereo.cas.web.support.RedisThrottledSubmissionHandlerInterceptorAdapter;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerConfigurationContext;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * This is {@link CasRedisThrottlingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration("casRedisThrottlingConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasRedisThrottlingConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("throttledRequestResponseHandler")
    private ObjectProvider<ThrottledRequestResponseHandler> throttledRequestResponseHandler;

    @Autowired
    @Qualifier("throttledRequestExecutor")
    private ObjectProvider<ThrottledRequestExecutor> throttledRequestExecutor;

    @Bean
    @ConditionalOnMissingBean(name = "redisThrottleConnectionFactory")
    public RedisConnectionFactory redisThrottleConnectionFactory() {
        val redis = casProperties.getAudit().getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis);
    }

    @Bean
    @ConditionalOnMissingBean(name = "throttleRedisTemplate")
    public RedisTemplate throttleRedisTemplate() {
        return RedisObjectFactory.newRedisTemplate(redisThrottleConnectionFactory());
    }

    @Autowired
    @Bean
    @RefreshScope
    public ThrottledSubmissionHandlerInterceptor authenticationThrottle(
        @Qualifier("auditTrailExecutionPlan") final AuditTrailExecutionPlan auditTrailExecutionPlan) {
        val throttle = casProperties.getAuthn().getThrottle();
        val failure = throttle.getFailure();

        val context = ThrottledSubmissionHandlerConfigurationContext.builder()
            .failureThreshold(failure.getThreshold())
            .failureRangeInSeconds(failure.getRangeSeconds())
            .usernameParameter(throttle.getUsernameParameter())
            .authenticationFailureCode(failure.getCode())
            .auditTrailExecutionPlan(auditTrailExecutionPlan)
            .applicationCode(throttle.getAppCode())
            .throttledRequestResponseHandler(throttledRequestResponseHandler.getObject())
            .throttledRequestExecutor(throttledRequestExecutor.getObject())
            .build();
        return new RedisThrottledSubmissionHandlerInterceptorAdapter(context, throttleRedisTemplate());
    }
}
