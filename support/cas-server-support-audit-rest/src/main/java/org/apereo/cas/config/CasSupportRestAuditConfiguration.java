package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailExecutionPlanConfigurer;
import org.apereo.cas.audit.RestAuditTrailManager;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasSupportRestAuditConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration("casSupportRestAuditConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasSupportRestAuditConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @ConditionalOnMissingBean(name = "restAuditTrailManager")
    @RefreshScope
    public AuditTrailManager restAuditTrailManager() {
        val rest = casProperties.getAudit().getRest();
        return new RestAuditTrailManager(rest);
    }

    @Bean
    @ConditionalOnMissingBean(name = "restAuditTrailExecutionPlanConfigurer")
    public AuditTrailExecutionPlanConfigurer restAuditTrailExecutionPlanConfigurer() {
        return plan -> plan.registerAuditTrailManager(restAuditTrailManager());
    }
}
