/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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
 * Provides status information about the number of SSO sessions established in CAS.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public class SessionStatus extends Status {
    /** Total number of SSO sessions maintained by CAS. */
    private final int sessionCount;

    /** Total number of service tickets in CAS ticket registry. */
    private final int serviceTicketCount;

    /**
     * Creates a new status object with the given code.
     *
     * @param code Status code.
     * @param desc Human-readable status description.
     *
     * @see #getCode()
     */
    public SessionStatus(final StatusCode code, final String desc) {
        this(code, desc, 0, 0);
    }


    /**
     * Creates a new status object with the given code.
     *
     * @param code Status code.
     * @param desc Human-readable status description.
     * @param sessions Number of established SSO sessions in ticket registry.
     * @param serviceTickets Number of service tickets in ticket registry.
     *
     * @see #getCode()
     */
     public SessionStatus(final StatusCode code, final String desc, final int sessions, final int serviceTickets) {
        super(code, desc);
        this.sessionCount = sessions;
        this.serviceTicketCount = serviceTickets;
    }


    /**
     * Gets total number of SSO sessions maintained by CAS.
     *
     * @return Total number of SSO sessions.
     */
    public int getSessionCount() {
        return this.sessionCount;
    }


    /**
     * Gets the total number of service tickets in the CAS ticket registry.
     *
     * @return Total number of service tickets.
     */
    public int getServiceTicketCount() {
        return this.serviceTicketCount;
    }
}
