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
package org.jasig.cas.logout;

import java.io.Serializable;

import org.jasig.cas.authentication.principal.SingleLogoutService;

/**
 * Define a logout request for a service accessed by a user.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
public final class LogoutRequest implements Serializable {

    /** Generated serialVersionUID. */
    private static final long serialVersionUID = -6411421298859045022L;

    /** The service ticket id. */
    private final String ticketId;

    /** The service. */
    private final SingleLogoutService service;

    /** The status of the logout request. */
    private LogoutRequestStatus status = LogoutRequestStatus.NOT_ATTEMPTED;

    /**
     * Build a logout request from ticket identifier and service.
     * Default status is {@link LogoutRequestStatus#NOT_ATTEMPTED}.
     *
     * @param ticketId the service ticket id.
     * @param service the service.
     */
    public LogoutRequest(final String ticketId, final SingleLogoutService service) {
        this.ticketId = ticketId;
        this.service = service;
    }

    public LogoutRequestStatus getStatus() {
        return status;
    }

    public void setStatus(final LogoutRequestStatus status) {
        this.status = status;
    }

    public String getTicketId() {
        return ticketId;
    }

    public SingleLogoutService getService() {
        return service;
    }
}
