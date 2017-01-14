package org.apereo.cas.monitor;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Simple health check monitor that reports the overall health as the greatest reported
 * {@link StatusCode} of an arbitrary number of individual checks.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public class HealthCheckMonitor implements Monitor<HealthStatus> {
    /**
     * Individual monitors that comprise health check.
     */
    private final Set<Monitor> monitors;

    public HealthCheckMonitor(final Set<Monitor> monitors) {
        this.monitors = monitors;
    }

    @Override
    public String getName() {
        return HealthCheckMonitor.class.getSimpleName();
    }

    @Override
    public HealthStatus observe() {
        final Map<String, Status> results = this.monitors.stream()
                .collect(Collectors.toMap(Monitor::getName, HealthCheckMonitor::getResultOf, (v1, v2) -> {
                    throw new IllegalStateException(String.format("Duplicate key %s", v1));
                }, LinkedHashMap::new));

        return new HealthStatus(getWorstStatusFrom(results), results);
    }

    private static StatusCode getWorstStatusFrom(final Map<String, Status> results) {
        return results.values().stream()
                .map(Status::getCode)
                .max(Comparator.comparingInt(StatusCode::value))
                .orElse(StatusCode.UNKNOWN);
    }

    private static Status getResultOf(final Monitor monitor) {
        try {
            return monitor.observe();
        } catch (final Exception e) {
            return new Status(StatusCode.ERROR, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
