package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.monitor.ExecutableObserver;
import org.apereo.cas.monitor.MonitorableTask;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.support.WebUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.tracing.ConditionalOnEnabledTracing;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.webflow.context.ExternalContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link CasWebflowMonitoringConfiguration}.
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
@Lazy(false)
@ConditionalOnEnabledTracing
public class CasWebflowMonitoringConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "casWebflowMonitoringAspect")
    public CasWebflowMonitoringAspect casWebflowMonitoringAspect(final ObjectProvider<ExecutableObserver> observer) {
        return new CasWebflowMonitoringAspect(observer);
    }

    @Aspect
    @Slf4j
    @SuppressWarnings("UnusedMethod")
    record CasWebflowMonitoringAspect(ObjectProvider<ExecutableObserver> observerProvider) {

        @Around("allComponentsThatAreWebflowExecutors()")
        public Object aroundWebflowOperations(final ProceedingJoinPoint joinPoint) throws Throwable {
            val observer = observerProvider.getObject();
            val flowId = joinPoint.getArgs()[0].toString();
            val externalContext = (ExternalContext) joinPoint.getArgs()[2];
            val httpRequest = (HttpServletRequest) externalContext.getNativeRequest();
            val taskName = joinPoint.getSignature().getDeclaringTypeName() + '.' + flowId;
            httpRequest.setAttribute("observingWebflowId", flowId);
            val task = new MonitorableTask(taskName)
                .withBoundedValue("flowId", flowId)
                .withBoundedValue("url", WebUtils.getHttpRequestFullUrl(httpRequest));
            return observer.supply(task, () -> executeJoinpoint(joinPoint));
        }

        private static Object executeJoinpoint(final ProceedingJoinPoint joinPoint) {
            return FunctionUtils.doUnchecked(() -> {
                var args = joinPoint.getArgs();
                LOGGER.trace("Executing [{}]", joinPoint.getStaticPart().toLongString());
                return joinPoint.proceed(args);
            });
        }

        @Pointcut("within(org.springframework.webflow.executor.FlowExecutor+) && execution(* launchExecution(String,*,org.springframework.webflow.context.ExternalContext,..))")
        private void allComponentsThatAreWebflowExecutors() {
        }

    }
}
