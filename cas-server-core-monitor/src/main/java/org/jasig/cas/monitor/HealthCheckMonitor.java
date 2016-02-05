package org.jasig.cas.monitor;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
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
@Component("healthCheckMonitor")
public class HealthCheckMonitor implements Monitor<HealthStatus> {
    /** Individual monitors that comprise health check. */
    @NotNull

    private Collection<Monitor> monitors = Collections.emptySet();


    /**
     * Sets the monitors that comprise the health check.
     *
     * @param monitors Collection of monitors responsible for observing various aspects of CAS.
     */
    @Resource(name="monitorsList")
    public void setMonitors(final Collection<Monitor> monitors) {
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
