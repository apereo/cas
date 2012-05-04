/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */
package org.jasig.cas.monitor;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple health check monitor that reports the overall health as the greatest reported
 * {@link StatusCode} of an arbitrary number of individual checks.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public class HealthCheckMonitor implements Monitor<HealthStatus> {
    /** Invidual monitors that comprise health check. */
    @NotNull
    private Collection<Monitor> monitors = Collections.emptySet();


    /**
     * Sets the monitors that comprise the health check.
     *
     * @param monitors Collection of monitors responsible for observing various aspects of CAS.
     */
    public void setMonitors(final Collection<Monitor> monitors) {
        this.monitors = monitors;
    }


    /** {@inheritDoc} */
    public String getName() {
        return HealthCheckMonitor.class.getSimpleName();
    }


    /** {@inheritDoc} */
    public HealthStatus observe() {
        final Map<String, Status> results = new HashMap<String, Status>(monitors.size());
        StatusCode code = StatusCode.UNKNOWN;
        Status result;
        for (Monitor monitor : monitors) {
            result = monitor.observe();
            if (result.getCode().value() > code.value()) {
                code = result.getCode();
            }
            results.put(monitor.getName(), result);
        }

        return new HealthStatus(code, results);
    }
}
