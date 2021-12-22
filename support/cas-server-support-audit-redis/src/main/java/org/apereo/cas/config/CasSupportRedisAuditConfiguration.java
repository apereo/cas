package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailExecutionPlanConfigurer;
import org.apereo.cas.audit.RedisAuditTrailManager;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;

import lombok.val;
import org.apereo.inspektr.audit.AuditTrailManager;
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
 * This is {@link CasSupportRedisAuditConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration(value = "CasSupportRedisAuditConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.audit.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CasSupportRedisAuditConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "redisAuditTrailManager")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuditTrailManager redisAuditTrailManager(
        @Qualifier("auditRedisTemplate")
        final RedisTemplate auditRedisTemplate,
        final CasConfigurationProperties casProperties) {
        val redis = casProperties.getAudit().getRedis();
        return new RedisAuditTrailManager(auditRedisTemplate, redis.isAsynchronous(), redis.getScanCount());
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisAuditConnectionFactory")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public RedisConnectionFactory redisAuditConnectionFactory(
        @Qualifier(CasSSLContext.BEAN_NAME)
        final CasSSLContext casSslContext,
        final CasConfigurationProperties casProperties) {
        val redis = casProperties.getAudit().getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis, casSslContext);
    }

    @Bean
    @ConditionalOnMissingBean(name = "auditRedisTemplate")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public RedisTemplate auditRedisTemplate(
        @Qualifier("redisAuditConnectionFactory")
        final RedisConnectionFactory redisAuditConnectionFactory) {
        return RedisObjectFactory.newRedisTemplate(redisAuditConnectionFactory);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuditTrailExecutionPlanConfigurer redisAuditTrailExecutionPlanConfigurer(
        @Qualifier("redisAuditTrailManager")
        final AuditTrailManager redisAuditTrailManager) {
        return plan -> plan.registerAuditTrailManager(redisAuditTrailManager);
    }
}
