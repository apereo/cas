package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailExecutionPlanConfigurer;
import org.apereo.cas.audit.RedisAuditTrailManager;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.redis.core.RedisObjectFactory;

import lombok.val;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
@Configuration("casSupportRedisAuditConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasSupportRedisAuditConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @ConditionalOnMissingBean(name = "redisAuditTrailManager")
    public AuditTrailManager redisAuditTrailManager() {
        val redis = casProperties.getAudit().getRedis();
        return new RedisAuditTrailManager(auditRedisTemplate(), redis.isAsynchronous());
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisAuditConnectionFactory")
    public RedisConnectionFactory redisAuditConnectionFactory() {
        val redis = casProperties.getAudit().getRedis();
        return RedisObjectFactory.newRedisConnectionFactory(redis);
    }

    @Bean
    @ConditionalOnMissingBean(name = "auditRedisTemplate")
    public RedisTemplate auditRedisTemplate() {
        return RedisObjectFactory.newRedisTemplate(redisAuditConnectionFactory());
    }

    @Bean
    public AuditTrailExecutionPlanConfigurer redisAuditTrailExecutionPlanConfigurer() {
        return plan -> plan.registerAuditTrailManager(redisAuditTrailManager());
    }
}
