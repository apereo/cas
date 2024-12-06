package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailExecutionPlanConfigurer;
import org.apereo.cas.audit.RedisAuditTrailManager;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.val;
import org.apereo.inspektr.audit.AuditTrailManager;
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
 * This is {@link CasSupportRedisAuditAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Audit, module = "redis")
@AutoConfiguration
public class CasSupportRedisAuditAutoConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.audit.redis.enabled").isTrue().evenIfMissing();

    @Bean
    @ConditionalOnMissingBean(name = "redisAuditTrailManager")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuditTrailManager redisAuditTrailManager(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("auditRedisTemplate")
        final CasRedisTemplate auditRedisTemplate,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(AuditTrailManager.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val redis = casProperties.getAudit().getRedis();
                return new RedisAuditTrailManager(auditRedisTemplate, redis.isAsynchronous());
            })
            .otherwiseProxy()
            .get();
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisAuditConnectionFactory")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public RedisConnectionFactory redisAuditConnectionFactory(
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
    @ConditionalOnMissingBean(name = "auditRedisTemplate")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasRedisTemplate auditRedisTemplate(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("redisAuditConnectionFactory")
        final RedisConnectionFactory redisAuditConnectionFactory) {
        return BeanSupplier.of(CasRedisTemplate.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> RedisObjectFactory.newRedisTemplate(redisAuditConnectionFactory))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "redisAuditTrailExecutionPlanConfigurer")
    public AuditTrailExecutionPlanConfigurer redisAuditTrailExecutionPlanConfigurer(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("redisAuditTrailManager")
        final AuditTrailManager redisAuditTrailManager) {
        return BeanSupplier.of(AuditTrailExecutionPlanConfigurer.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> plan -> plan.registerAuditTrailManager(redisAuditTrailManager))
            .otherwiseProxy()
            .get();
    }
}
