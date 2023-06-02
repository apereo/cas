package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.monitor.ExecutableObserver;
import org.apereo.cas.monitor.MonitorableTask;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * This is {@link CasCoreServicesMonitoringConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = {
    CasFeatureModule.FeatureCatalog.Monitoring,
    CasFeatureModule.FeatureCatalog.TicketRegistry
})
@ConditionalOnBean(name = ExecutableObserver.BEAN_NAME)
@AutoConfiguration
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
public class CasCoreServicesMonitoringConfiguration {
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
            val observer = observerProvider.getObject();
            val taskName = joinPoint.getSignature().getDeclaringTypeName() + '.' + joinPoint.getSignature().getName();
            val task = new MonitorableTask(taskName);
            return observer.supply(task, () -> executeJoinpoint(joinPoint));
        }

        private static Object executeJoinpoint(final ProceedingJoinPoint joinPoint) {
            return FunctionUtils.doUnchecked(() -> {
                var args = joinPoint.getArgs();
                LOGGER.trace("Executing [{}]", joinPoint.getStaticPart().toLongString());
                return joinPoint.proceed(args);
            });
        }

        @Pointcut("within(org.apereo.cas.services.mgmt.*)")
        private void allComponentsInServiceManagementNamespace() {
        }
    }
}
