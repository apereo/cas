package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.monitor.ExecutableObserver;
import org.apereo.cas.monitor.Monitorable;
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
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * This is {@link CasCoreAuthenticationMonitoringConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = {
    CasFeatureModule.FeatureCatalog.Monitoring,
    CasFeatureModule.FeatureCatalog.Authentication
})
@ConditionalOnBean(name = ExecutableObserver.BEAN_NAME)
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
@EnableAspectJAutoProxy
public class CasCoreAuthenticationMonitoringConfiguration {
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
            if (AnnotationUtils.findAnnotation(joinPoint.getThis().getClass(), Monitorable.class) != null) {
                val observer = observerProvider.getObject();
                val taskName = joinPoint.getSignature().getDeclaringTypeName() + '.' + joinPoint.getSignature().getName();
                val task = new MonitorableTask(taskName);
                return observer.supply(task, () -> executeJoinpoint(joinPoint));
            }
            return executeJoinpoint(joinPoint);
        }

        private static Object executeJoinpoint(final ProceedingJoinPoint joinPoint) {
            return FunctionUtils.doUnchecked(() -> {
                var args = joinPoint.getArgs();
                LOGGER.trace("Executing [{}]", joinPoint.getStaticPart().toLongString());
                return joinPoint.proceed(args);
            });
        }

        @Pointcut("within(org.apereo.cas.authentication.AuthenticationManager+) && execution(* authenticate(..))")
        private void allComponentsInAuthenticationManagementNamespace() {
        }
    }
}
