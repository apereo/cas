package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailExecutionPlanConfigurer;
import org.apereo.cas.audit.RedisAuditTrailManager;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;

import lombok.val;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * This is {@link CasSupportRedisAuditConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration(value = "casSupportRedisAuditConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.audit.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CasSupportRedisAuditConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "redisAuditTrailManager")
    @Autowired
    public AuditTrailManager redisAuditTrailManager(
        @Qualifier("auditRedisTemplate")
        final RedisTemplate auditRedisTemplate,
        final CasConfigurationProperties casProperties) {
        val redis = casProperties.getAudit().getRedis();
        return new RedisAuditTrailManager(auditRedisTemplate, redis.isAsynchronous());
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisAuditConnectionFactory")
    @Autowired
    public RedisConnectionFactory redisAuditConnectionFactory(final CasConfigurationProperties casProperties) {
        val redis = casProperties.getAudit().getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis);
    }

    @Bean
    @Autowired
    @ConditionalOnMissingBean(name = "auditRedisTemplate")
    public RedisTemplate auditRedisTemplate(
        @Qualifier("redisAuditConnectionFactory")
        final RedisConnectionFactory redisAuditConnectionFactory) {
        return RedisObjectFactory.newRedisTemplate(redisAuditConnectionFactory);
    }

    @Bean
    @Autowired
    public AuditTrailExecutionPlanConfigurer redisAuditTrailExecutionPlanConfigurer(
        @Qualifier("redisAuditTrailManager")
        final AuditTrailManager redisAuditTrailManager) {
        return plan -> plan.registerAuditTrailManager(redisAuditTrailManager);
    }
}
