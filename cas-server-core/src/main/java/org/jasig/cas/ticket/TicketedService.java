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
package org.jasig.cas.ticket;

import java.io.Serializable;

import org.jasig.cas.authentication.principal.Service;

/**
 * Simple container for a service-ticket pair.
 *
 * @author Marvin S. Addison
 */
public final class TicketedService implements Serializable {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 2351511372236270830L;

    /**
     * Ticket granted to service.
     */
    private final String ticketId;

    /**
     * Service accessed during CAS session.
     */
    private final Service service;

    /**
     * Build from a service ticket and service.
     *
     * @param ticketId the service ticket identifier.
     * @param service the service.
     */
    public TicketedService(final String ticketId, final Service service) {
        this.ticketId = ticketId;
        this.service = service;
    }

    /**
     * Get the service ticket.
     *
     * @return the service ticket.
     */
    public String getTicketId() {
        return ticketId;
    }

    /**
     * Get the service.
     *
     * @return the service.
     */
    public Service getService() {
        return service;
    }
}
