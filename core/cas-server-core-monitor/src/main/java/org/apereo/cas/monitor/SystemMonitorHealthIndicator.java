package org.apereo.cas.monitor;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;

import java.util.List;

/**
 * Monitors JVM system load and memory.
 *
 * @author Travis Schmidt
 * @since 6.1.0
 */
@RequiredArgsConstructor
public class SystemMonitorHealthIndicator extends AbstractHealthIndicator {

    private final MetricsEndpoint metrics;
    private final int threshold;

    @Override
    protected void doHealthCheck(final Health.Builder builder) {
        val systemUsage = metrics.metric("system.cpu.usage", null).getMeasurements().get(0).getValue();
        val systemLoad = metrics.metric("system.load.average.1m", null).getMeasurements().get(0).getValue();
        val processUsage = metrics.metric("process.cpu.usage", null).getMeasurements().get(0).getValue();
        val jvmUsed = metrics.metric("jvm.memory.used", null).getMeasurements().get(0).getValue();
        val jvmCommitted = metrics.metric("jvm.memory.committed", null).getMeasurements().get(0).getValue();
        val heapUsed = metrics.metric("jvm.memory.used", List.of("area:heap")).getMeasurements().get(0).getValue();
        val heapCommitted = metrics.metric("jvm.memory.committed", List.of("area:heap")).getMeasurements().get(0).getValue();
        val uptime = metrics.metric("process.uptime", null).getMeasurements().get(0).getValue();
        val reqs = metrics.metric("http.server.requests", null);

        builder
                .withDetail("systemUsage", systemUsage)
                .withDetail("systemLoad", systemLoad)
                .withDetail("processUsage", processUsage)
                .withDetail("jvmUsed", jvmUsed)
                .withDetail("jvmCommitted", jvmCommitted)
                .withDetail("heapUsed", heapUsed)
                .withDetail("heapCommitted", heapCommitted)
                .withDetail("uptime", uptime)
                .withDetail("requests", reqs != null ? reqs.getMeasurements().get(0).getValue() : 0)
                .withDetail("maxRequest", reqs != null ? reqs.getMeasurements().get(2).getValue() : 0);

        if (systemLoad > threshold) {
            builder.status("WARN");
        } else {
            builder.status(Status.UP);
        }
    }
}
