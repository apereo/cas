package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.throttle.ThrottledSubmissionHandlerConfigurationContext;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.support.RedisThrottledSubmissionHandlerInterceptorAdapter;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * This is {@link CasRedisThrottlingAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Throttling, module = "redis")
@AutoConfiguration
public class CasRedisThrottlingAutoConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.audit.redis.enabled").isTrue().evenIfMissing();

    @Bean
    @ConditionalOnMissingBean(name = "redisThrottleConnectionFactory")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public RedisConnectionFactory redisThrottleConnectionFactory(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasSSLContext.BEAN_NAME)
        final CasSSLContext casSslContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(RedisConnectionFactory.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(Unchecked.supplier(() -> {
                val redis = casProperties.getAudit().getRedis();
                return RedisObjectFactory.newRedisConnectionFactory(redis, casSslContext);
            }))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @ConditionalOnMissingBean(name = "throttleRedisTemplate")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasRedisTemplate throttleRedisTemplate(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("redisThrottleConnectionFactory")
        final RedisConnectionFactory redisThrottleConnectionFactory) {
        return BeanSupplier.of(CasRedisTemplate.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> RedisObjectFactory.newRedisTemplate(redisThrottleConnectionFactory))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ThrottledSubmissionHandlerInterceptor authenticationThrottle(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("throttleRedisTemplate")
        final CasRedisTemplate throttleRedisTemplate,
        @Qualifier("authenticationThrottlingConfigurationContext")
        final ThrottledSubmissionHandlerConfigurationContext authenticationThrottlingConfigurationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(ThrottledSubmissionHandlerInterceptor.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new RedisThrottledSubmissionHandlerInterceptorAdapter(authenticationThrottlingConfigurationContext,
                throttleRedisTemplate,
                casProperties.getAudit().getRedis().getScanCount()))
            .otherwise(ThrottledSubmissionHandlerInterceptor::noOp)
            .get();
    }
}
