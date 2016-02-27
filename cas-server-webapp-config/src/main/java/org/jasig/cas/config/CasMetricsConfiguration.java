package org.jasig.cas.config;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.google.common.collect.ImmutableSet;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;
import org.jasig.cas.security.RequestParameterPolicyEnforcementFilter;
import org.jasig.cas.security.ResponseHeadersEnforcementFilter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link CasMetricsConfiguration} that attempts to create Spring-managed beans
 * backed by external configuration.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Configuration("casMetricsConfiguration")
@EnableMetrics
public class CasMetricsConfiguration extends MetricsConfigurerAdapter {

    @Value("${metrics.refresh.internal:30}")
    private long perfStatsPeriod;

    @Autowired
    @Qualifier("perfStatsLogger")
    private Logger perfStatsLogger;
    
    @Override
    public MetricRegistry getMetricRegistry() {
        final MetricRegistry metrics = new MetricRegistry();
        metrics.register("jvm.gc", new GarbageCollectorMetricSet());
        metrics.register("jvm.memory", new MemoryUsageGaugeSet());
        metrics.register("thread-states", new ThreadStatesGaugeSet());
        metrics.register("jvm.fd.usage", new FileDescriptorRatioGauge());
        return metrics;
    }

    @Override
    public HealthCheckRegistry getHealthCheckRegistry() {
        return new HealthCheckRegistry();
    }

    @Override
    public void configureReporters(final MetricRegistry metricRegistry) {
        registerReporter(Slf4jReporter
                .forRegistry(metricRegistry)
                .outputTo(this.perfStatsLogger)
                .convertRatesTo(TimeUnit.MILLISECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build())
                .start(this.perfStatsPeriod, TimeUnit.SECONDS);

        registerReporter(JmxReporter
                .forRegistry(metricRegistry)
                .build());
        
    }
}
