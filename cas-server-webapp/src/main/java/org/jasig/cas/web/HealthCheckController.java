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
package org.jasig.cas.web;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.jasig.cas.monitor.HealthCheckMonitor;
import org.jasig.cas.monitor.HealthStatus;
import org.jasig.cas.monitor.Status;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Reports overall CAS health based on the observations of the configured {@link HealthCheckMonitor} instance.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public class HealthCheckController extends AbstractController {

    /** Prefix for custom response headers with health check details. */
    private static final String HEADER_PREFIX = "X-CAS-";

    @NotNull
    private HealthCheckMonitor healthCheckMonitor;


    /**
     * Sets the health check monitor used to observe system health.
     * @param monitor Health monitor configured with subordinate monitors that observe specific aspects of overall
     *                system health.
     */
    public void setHealthCheckMonitor(final HealthCheckMonitor monitor) {
        this.healthCheckMonitor = monitor;
    }


    /** {@inheritDoc} */
    protected ModelAndView handleRequestInternal(
            final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final HealthStatus healthStatus = this.healthCheckMonitor.observe();
        final StringBuilder sb = new StringBuilder();
        sb.append("Health: ").append(healthStatus.getCode());
        String name;
        Status status;
        int i = 0;
        for (final Map.Entry<String, Status> entry : healthStatus.getDetails().entrySet()) {
            name = entry.getKey();
            status = entry.getValue();
            response.addHeader("X-CAS-" + name, String.format("%s;%s", status.getCode(), status.getDescription()));

            sb.append("\n\n\t").append(++i).append('.').append(name).append(": ");
            sb.append(status.getCode());
            if (status.getDescription() != null) {
                sb.append(" - ").append(status.getDescription());
            }
        }
        response.setStatus(healthStatus.getCode().value());
        response.setContentType("text/plain");
        response.getOutputStream().write(sb.toString().getBytes(response.getCharacterEncoding()));

        // Return null to signal MVC framework that we handled response directly
        return null;
    }
}
