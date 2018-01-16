package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.audit.AuditTrailExecutionPlanConfigurer;
import org.apereo.cas.audit.RestAuditTrailManager;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@Slf4j
public class CasSupportRestAuditConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public AuditTrailManager restAuditTrailManager() {
        return new RestAuditTrailManager(casProperties.getAudit().getRest());
    }

    @Bean
    public AuditTrailExecutionPlanConfigurer restAuditTrailExecutionPlanConfigurer() {
        return plan -> plan.registerAuditTrailManager(restAuditTrailManager());
    }
}
