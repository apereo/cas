package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.monitor.DefaultExecutableObserver;
import org.apereo.cas.monitor.ExecutableObserver;
import org.apereo.cas.monitor.MemoryMonitorHealthIndicator;
import org.apereo.cas.monitor.SlowRequestsFilter;
import org.apereo.cas.monitor.SystemMonitorHealthIndicator;
import org.apereo.cas.monitor.TicketRegistryHealthIndicator;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.ObservationTextPublisher;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.health.autoconfigure.contributor.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.micrometer.metrics.actuate.endpoint.MetricsEndpoint;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;

/**
 * This is {@link CasCoreMonitorAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Monitoring)
@AutoConfiguration
@EnableAspectJAutoProxy(proxyTargetClass = false)
public class CasCoreMonitorAutoConfiguration {

    @Configuration(value = "ObservationsConfiguration", proxyBeanMethods = false)
    static class ObservationsConfiguration {
        @ConditionalOnMissingBean(name = ExecutableObserver.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ExecutableObserver defaultExecutableObserver(final ObjectProvider<@NonNull ObservationRegistry> observationRegistry) {
            return new DefaultExecutableObserver(observationRegistry);
        }

        @Bean
        public ObservationHandler<Observation.@NonNull Context> observationTextPublisher() {
            return new ObservationTextPublisher();
        }
    }
    
    @Configuration(value = "MemoryMonitorHealthIndicatorConfiguration", proxyBeanMethods = false)
    static class MemoryMonitorHealthIndicatorConfiguration {
        @ConditionalOnMissingBean(name = "memoryHealthIndicator")
        @Bean
        @ConditionalOnEnabledHealthIndicator("memoryHealthIndicator")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public HealthIndicator memoryHealthIndicator(
            final CasConfigurationProperties casProperties) {
            val freeMemThreshold = casProperties.getMonitor().getMemory().getFreeMemThreshold();
            if (freeMemThreshold > 0) {
                LOGGER.debug("Configured memory monitor with free-memory threshold [{}]", freeMemThreshold);
                return new MemoryMonitorHealthIndicator(freeMemThreshold);
            }
            return () -> Health.up().build();
        }

        @ConditionalOnMissingBean(name = "sessionHealthIndicator")
        @Bean
        @ConditionalOnEnabledHealthIndicator("sessionHealthIndicator")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public HealthIndicator sessionHealthIndicator(
            @Qualifier(TicketRegistry.BEAN_NAME)
            final ObjectProvider<@NonNull TicketRegistry> ticketRegistry,
            final CasConfigurationProperties casProperties) {
            val warnSt = casProperties.getMonitor().getSt().getWarn();
            val warnTgt = casProperties.getMonitor().getTgt().getWarn();
            if (warnSt.getThreshold() > 0 && warnTgt.getThreshold() > 0) {
                LOGGER.debug("Configured session monitor with service ticket threshold [{}] and session threshold [{}]",
                    warnSt.getThreshold(), warnTgt.getThreshold());
                return new TicketRegistryHealthIndicator(ticketRegistry, warnSt.getThreshold(), warnTgt.getThreshold());
            }
            return () -> Health.up().build();
        }
    }

    @Configuration(value = "SystemHealthIndicatorConfiguration", proxyBeanMethods = false)
    static class SystemHealthIndicatorConfiguration {
        @ConditionalOnMissingBean(name = "systemHealthIndicator")
        @Bean
        @ConditionalOnEnabledHealthIndicator("systemHealthIndicator")
        @ConditionalOnAvailableEndpoint(endpoint = MetricsEndpoint.class)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public HealthIndicator systemHealthIndicator(
            @Qualifier("metricsEndpoint") final ObjectProvider<@NonNull MetricsEndpoint> metricsEndpoint,
            final CasConfigurationProperties casProperties) {
            val warnLoad = casProperties.getMonitor().getLoad().getWarn();
            return new SystemMonitorHealthIndicator(metricsEndpoint, warnLoad.getThreshold());
        }


        @ConditionalOnMissingBean(name = "slowRequestsFilter")
        @Bean
        @ConditionalOnEnabledHealthIndicator("systemHealthIndicator")
        @ConditionalOnAvailableEndpoint(endpoint = MetricsEndpoint.class)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FilterRegistrationBean slowRequestsFilter(final MeterRegistry meterRegistry) {
            val slowRequestTimer = Timer.builder("slow.requests.timer")
                .publishPercentileHistogram()
                .register(meterRegistry);
            val filter = new SlowRequestsFilter(slowRequestTimer);
            val bean = new FilterRegistrationBean<@NonNull SlowRequestsFilter>();
            bean.setFilter(filter);
            bean.setAsyncSupported(true);
            bean.setUrlPatterns(CollectionUtils.wrap("/*"));
            bean.setName("slowRequestsFilter");
            bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
            return bean;
        }
    }

}
