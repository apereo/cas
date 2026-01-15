package org.apereo.cas.monitor;

import module java.base;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.boot.micrometer.metrics.actuate.endpoint.MetricsEndpoint;

/**
 * Monitors JVM system load and memory.
 *
 * @author Travis Schmidt
 * @since 6.1.0
 */
@RequiredArgsConstructor
public class SystemMonitorHealthIndicator extends AbstractHealthIndicator {

    private final ObjectProvider<MetricsEndpoint> metrics;

    private final int threshold;

    @Override
    protected void doHealthCheck(final Health.Builder builder) {
        val systemLoad = getMetricsFor("system.load.average.1m");

        builder
            .withDetail("systemUsage", getMetricsFor("system.cpu.usage"))
            .withDetail("systemLoad", getMetricsFor("system.load.average.1m"))
            .withDetail("processUsage", getMetricsFor("process.cpu.usage"))
            .withDetail("jvmUsed", getMetricsFor("jvm.memory.used"))
            .withDetail("jvmCommitted", getMetricsFor("jvm.memory.committed"))
            .withDetail("heapUsed", getMetricsFor("jvm.memory.used", List.of("area:heap")))
            .withDetail("heapCommitted", getMetricsFor("jvm.memory.committed", List.of("area:heap")))
            .withDetail("uptime", getMetricsFor("process.uptime"))
            .withDetail("requests", getMetricsFor("http.server.requests"))
            .withDetail("maxRequest", getMetricsFor("http.server.requests", 2));

        if (systemLoad > threshold) {
            builder.status("WARN");
        } else {
            builder.status(Status.UP);
        }
    }

    private double getMetricsFor(final String key) {
        return getMetricsFor(key, null, 0);
    }

    private double getMetricsFor(final String key, final int measure) {
        return getMetricsFor(key, null, measure);
    }

    private double getMetricsFor(final String key, final List<String> tag) {
        return getMetricsFor(key, tag, 0);
    }

    private double getMetricsFor(final String key, final @Nullable List<String> tag, final int measure) {
        return Optional.ofNullable(metrics.getIfAvailable())
            .map(endpoint -> {
                val metric = endpoint.metric(key, tag);
                val measures = Optional.ofNullable(metric).map(MetricsEndpoint.MetricDescriptor::getMeasurements).orElse(null);
                return measures != null ? measures.get(measure).getValue() : 0;
            })
            .orElse(0.0D);
    }
}
