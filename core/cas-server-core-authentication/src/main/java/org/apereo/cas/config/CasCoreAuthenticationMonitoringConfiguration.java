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
 * This is {@link CasCoreAuthenticationMonitoringConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Configuration(value = "CasCoreAuthenticationMonitoringConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = {
    CasFeatureModule.FeatureCatalog.Monitoring,
    CasFeatureModule.FeatureCatalog.Authentication
})
@EnableAspectJAutoProxy
@Lazy(false)
@ConditionalOnEnabledTracing
class CasCoreAuthenticationMonitoringConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "authenticationManagerMonitoringAspect")
    public AuthenticationManagerMonitoringAspect authenticationManagerMonitoringAspect(final ObjectProvider<ExecutableObserver> observer) {
        return new AuthenticationManagerMonitoringAspect(observer);
    }

    @Aspect
    @Slf4j
    @SuppressWarnings("UnusedMethod")
    record AuthenticationManagerMonitoringAspect(ObjectProvider<ExecutableObserver> observerProvider) {

        @Around("allComponentsInAuthenticationManagementNamespace()")
        public Object aroundAuthenticationManagementOperations(final ProceedingJoinPoint joinPoint) throws Throwable {
            return ExecutableObserver.observe(observerProvider, joinPoint);
        }

        @Pointcut("within(org.apereo.cas.authentication.AuthenticationManager+) && execution(* authenticate(..))")
        private void allComponentsInAuthenticationManagementNamespace() {
        }
    }
}
