package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.monitor.ExecutableObserver;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.tracing.ConditionalOnEnabledTracing;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Lazy;

/**
 * This is {@link SamlIdPMonitoringConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Configuration(value = "SamlIdPMonitoringConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = {
    CasFeatureModule.FeatureCatalog.Monitoring,
    CasFeatureModule.FeatureCatalog.SAMLIdentityProvider
})
@EnableAspectJAutoProxy
@Lazy(false)
@ConditionalOnEnabledTracing
class SamlIdPMonitoringConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "samlIdPMonitoringAspect")
    public SamlIdPMonitoringAspect samlIdPMonitoringAspect(final ObjectProvider<ExecutableObserver> observer) {
        return new SamlIdPMonitoringAspect(observer);
    }

    @Aspect
    @Slf4j
    @SuppressWarnings("UnusedMethod")
    record SamlIdPMonitoringAspect(ObjectProvider<ExecutableObserver> observerProvider) {

        @Around("metadataComponentsInSamlIdPNamespace()")
        public Object aroundMetadataManagementOperations(final ProceedingJoinPoint joinPoint) throws Throwable {
            return ExecutableObserver.observe(observerProvider, joinPoint);
        }

        @Pointcut("within(org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver+)")
        private void metadataComponentsInSamlIdPNamespace() {
        }
    }
}
