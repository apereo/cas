/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.monitor;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;

/**
 * Simple health check monitor that reports the overall health as the greatest reported
 * {@link StatusCode} of an arbitrary number of individual checks.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public class HealthCheckMonitor implements Monitor<HealthStatus> {
    /** Individual monitors that comprise health check. */
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
    @Override
    public String getName() {
        return HealthCheckMonitor.class.getSimpleName();
    }

    /** {@inheritDoc} */
    @Override
    public HealthStatus observe() {
        final Map<String, Status> results = new LinkedHashMap<String, Status>(this.monitors.size());
        StatusCode code = StatusCode.UNKNOWN;
        Status result;
        for (final Monitor monitor : this.monitors) {
            try {
                result = monitor.observe();
                if (result.getCode().value() > code.value()) {
                    code = result.getCode();
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
