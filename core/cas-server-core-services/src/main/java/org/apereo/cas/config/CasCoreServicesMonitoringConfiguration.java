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
import org.springframework.context.annotation.Lazy;

/**
 * This is {@link CasCoreServicesMonitoringConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = {
    CasFeatureModule.FeatureCatalog.Monitoring,
    CasFeatureModule.FeatureCatalog.TicketRegistry
})
@Configuration(value = "CasCoreServicesMonitoringConfiguration", proxyBeanMethods = false)
@Lazy(false)
@ConditionalOnEnabledTracing
class CasCoreServicesMonitoringConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "servicesManagerMonitoringAspect")
    public ServicesManagerMonitoringAspect servicesManagerMonitoringAspect(final ObjectProvider<ExecutableObserver> observer) {
        return new ServicesManagerMonitoringAspect(observer);
    }

    @Aspect
    @Slf4j
    @SuppressWarnings("UnusedMethod")
    record ServicesManagerMonitoringAspect(ObjectProvider<ExecutableObserver> observerProvider) {

        @Around("allComponentsInServiceManagementNamespace()")
        public Object aroundServiceManagementOperations(final ProceedingJoinPoint joinPoint) throws Throwable {
            return ExecutableObserver.observe(observerProvider, joinPoint);
        }

        @Pointcut("within(org.apereo.cas.services.mgmt.*)")
        private void allComponentsInServiceManagementNamespace() {
        }
    }
}
