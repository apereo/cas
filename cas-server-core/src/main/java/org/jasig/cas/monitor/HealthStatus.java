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

import java.util.Collections;
import java.util.Map;

/**
 * Describes the overall health status of the CAS server as determined by composite status values.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public class HealthStatus extends Status {
    /** Map of names (e.g. monitor that produced it) to status information. */
    private final Map<String, Status> details;


    /**
     * Creates a new status object with the given code.
     *
     * @param code Status code.
     * @param detailMap Map of names to status information. A reasonable name would be, for example, the name of
     *                  the monitor that produced it.
     * @see #getCode()
     */
    public HealthStatus(final StatusCode code, final Map<String, Status> detailMap) {
        super(code);
        this.details = Collections.unmodifiableMap(detailMap);
    }


    /**
     * Gets the status details comprising the individual health checks performed for overall health status.
     *
     * @return Map of named status items to status information for each check performed.
     */
    public Map<String, Status> getDetails() {
        return this.details;
    }
}
