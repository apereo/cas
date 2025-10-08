package org.apereo.cas.config;

import org.apereo.cas.apm.ElasticApmMonitoringAspect;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.micrometer.tracing.autoconfigure.ConditionalOnEnabledTracingExport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * This is {@link CasElasticApmAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Monitoring, module = "elastic")
@AutoConfiguration
@EnableAspectJAutoProxy
@ConditionalOnEnabledTracingExport
public class CasElasticApmAutoConfiguration {

    @Configuration(value = "ElasticApmTracerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class ElasticApmTracerConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "elasticApmMonitoringAspect")
        @Lazy(false)
        public ElasticApmMonitoringAspect elasticApmMonitoringAspect() {
            return new ElasticApmMonitoringAspect();
        }
    }
}
