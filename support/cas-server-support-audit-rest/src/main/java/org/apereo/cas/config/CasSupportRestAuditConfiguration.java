package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailExecutionPlanConfigurer;
import org.apereo.cas.audit.RestAuditTrailManager;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasSupportRestAuditConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration(value = "casSupportRestAuditConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasSupportRestAuditConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "restAuditTrailManager")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public AuditTrailManager restAuditTrailManager(final CasConfigurationProperties casProperties) {
        val rest = casProperties.getAudit().getRest();
        return new RestAuditTrailManager(rest);
    }

    @Bean
    @Autowired
    @ConditionalOnMissingBean(name = "restAuditTrailExecutionPlanConfigurer")
    public AuditTrailExecutionPlanConfigurer restAuditTrailExecutionPlanConfigurer(
        @Qualifier("restAuditTrailManager")
        final AuditTrailManager restAuditTrailManager) {
        return plan -> plan.registerAuditTrailManager(restAuditTrailManager);
    }
}
