package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.monitor.ExecutableObserver;
import org.apereo.cas.monitor.MonitorableTask;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.jooq.lambda.Unchecked;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * This is {@link CasCoreTicketsMonitoringConfiguration}.
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
@AutoConfiguration
public class CasCoreTicketsMonitoringConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "ticketRegistryMonitoringAspect")
    public TicketRegistryMonitoringAspect ticketRegistryMonitoringAspect(final ExecutableObserver observer) {
        return new TicketRegistryMonitoringAspect(observer);
    }

    @Aspect
    @Slf4j
    @SuppressWarnings("UnusedMethod")
    record TicketRegistryMonitoringAspect(ExecutableObserver observer) {
        @Around("allComponentsInTicketRegistryNamespace()")
        public Object aroundAdvice(final ProceedingJoinPoint joinPoint) throws Throwable {
            val taskName = joinPoint.getSignature().getDeclaringTypeName() + '.' + joinPoint.getSignature().getName();
            val task = new MonitorableTask(taskName);
            return observer.supply(task, Unchecked.supplier(() -> {
                var args = joinPoint.getArgs();
                LOGGER.trace("Executing [{}]", joinPoint.getStaticPart().toLongString());
                return joinPoint.proceed(args);
            }));
        }

        @Pointcut("within(org.apereo.cas.ticket.registry.*)")
        private void allComponentsInTicketRegistryNamespace() {
        }
    }
}
