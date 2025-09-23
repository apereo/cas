package org.apereo.cas.config;

import org.apereo.cas.audit.AuditTrailExecutionPlanConfigurer;
import org.apereo.cas.audit.RestAuditTrailManager;
import org.apereo.cas.audit.spi.AuditActionContextJsonSerializer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.val;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasSupportRestAuditAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Audit, module = "rest")
@AutoConfiguration
public class CasSupportRestAuditAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "restAuditTrailManager")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuditTrailManager restAuditTrailManager(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val rest = casProperties.getAudit().getRest();
        return new RestAuditTrailManager(new AuditActionContextJsonSerializer(applicationContext), rest);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "restAuditTrailExecutionPlanConfigurer")
    public AuditTrailExecutionPlanConfigurer restAuditTrailExecutionPlanConfigurer(
        @Qualifier("restAuditTrailManager")
        final AuditTrailManager restAuditTrailManager) {
        return plan -> plan.registerAuditTrailManager(restAuditTrailManager);
    }
}
