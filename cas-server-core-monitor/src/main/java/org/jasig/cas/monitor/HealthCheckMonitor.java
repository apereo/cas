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
        StatusCode code = StatusCode.UNKNOWN;
        Status result;
        for (final Monitor monitor : this.monitors) {
            try {
                result = monitor.observe();
                final StatusCode resCode = result.getCode();
                if (resCode.value() > code.value()) {
                    code = resCode;
                }
            } catch (final Exception e) {
                code = StatusCode.ERROR;
                result = new Status(code, e.getClass().getSimpleName() + ": " + e.getMessage());
            }
            results.put(monitor.getName(), result);
        }

        return new HealthStatus(code, results);
    }
}
