package org.apereo.cas.config;


import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.sentry.SentryMonitoringAspect;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.actuate.autoconfigure.tracing.ConditionalOnEnabledTracing;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * This is {@link CasSentryAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Monitoring, module = "sentry")
@AutoConfiguration
@EnableAspectJAutoProxy(proxyTargetClass = false)
@ConditionalOnEnabledTracing
public class CasSentryAutoConfiguration {

    @Configuration(value = "SentryTracerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SentryTracerConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "sentryMonitoringAspect")
        @Lazy(false)
        public SentryMonitoringAspect sentryMonitoringAspect() {
            return new SentryMonitoringAspect();
        }
    }
}
