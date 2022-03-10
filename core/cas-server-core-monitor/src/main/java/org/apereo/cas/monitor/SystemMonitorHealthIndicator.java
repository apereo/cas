package org.apereo.cas.monitor;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;

import java.util.List;
import java.util.Optional;

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

    private double getMetricsFor(final String key, final List<String> tag, final int measure) {
        return Optional.ofNullable(metrics.getIfAvailable())
            .map(endpoint -> {
                val metric = endpoint.metric(key, tag);
                val measures = metric != null ? metric.getMeasurements() : null;
                return measures != null ? measures.get(measure).getValue() : 0;
            })
            .orElse(0D);
    }
}
