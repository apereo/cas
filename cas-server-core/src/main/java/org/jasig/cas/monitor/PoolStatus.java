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

/**
 * Describes the status of a resource pool.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public class PoolStatus extends Status {
    /** Number of idle pool resources. */
    private final int idleCount;

    /** Number of active pool resources. */
    private final int activeCount;


    /**
     * Creates a new status object with the given code.
     *
     * @param code Status code.
     * @param desc Human-readable status description.
     *
     * @see #getCode()
     */
    public PoolStatus(final StatusCode code, final String desc, final int active, final int idle) {
        super(code, buildDescription(desc, active, idle));
        this.activeCount = active;
        this.idleCount = idle;
    }


    /**
     * Gets the number of idle pool resources.
     *
     * @return Number of idle pool members.
     */
    public int getIdleCount() {
        return idleCount;
    }


    /**
     * Gets the number of active pool resources.
     *
     * @return Number of active pool members.
     */
    public int getActiveCount() {
        return activeCount;
    }
    
    
    private static String buildDescription(final String desc, final int active, final int idle) {
        final StringBuilder sb = new StringBuilder();
        if (desc != null) {
            sb.append(desc);
            if (!desc.endsWith(".")) {
                sb.append('.');
            }
            sb.append(' ');
        }
        sb.append(active).append(" active, ").append(idle).append(" idle.");
        return sb.toString();
    }
}
