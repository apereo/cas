package org.apereo.cas.monitor;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

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
    private Collection<Monitor> monitors = Collections.emptySet();

    public void setMonitors(final Collection monitors) {
        this.monitors = monitors;
    }

    @Override
    public String getName() {
        return HealthCheckMonitor.class.getSimpleName();
    }

    @Override
    public HealthStatus observe() {
        final Map<String, Status> results = new LinkedHashMap<>(this.monitors.size());
        final StatusCode[] code = {StatusCode.UNKNOWN};
        final Status[] result = new Status[1];
        this.monitors.stream().forEach(monitor -> {
            try {
                result[0] = monitor.observe();
                final StatusCode resCode = result[0].getCode();
                if (resCode.value() > code[0].value()) {
                    code[0] = resCode;
                }
            } catch (final Exception e) {
                code[0] = StatusCode.ERROR;
                result[0] = new Status(code[0], e.getClass().getSimpleName() + ": " + e.getMessage());
            }
            results.put(monitor.getName(), result[0]);
        });

        return new HealthStatus(code[0], results);
    }
}
