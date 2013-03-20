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
package org.jasig.cas.mock;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.UniqueTicketIdGenerator;

import java.util.Date;

/**
 * Mock service ticket.
 *
 * @author Marvin S. Addison
 */
public class MockServiceTicket implements ServiceTicket {
    private static final UniqueTicketIdGenerator ID_GENERATOR = new DefaultUniqueTicketIdGenerator();

    private final String id;

    private final Date created;

    private final Service service;

    private final TicketGrantingTicket parent;

    public MockServiceTicket(final String id, final Service service, final TicketGrantingTicket parent) {
        this.service = service;
        this.id = id;
        this.parent = parent;
        created = new Date();
    }

    public Service getService() {
        return service;
    }

    public boolean isFromNewLogin() {
        return false;
    }

    public boolean isValidFor(final Service service) {
        return this.service.equals(service);
    }

    public TicketGrantingTicket grantTicketGrantingTicket(
            final String id,
            final Authentication authentication,
            final ExpirationPolicy expirationPolicy) {
        return null;
    }

    public String getId() {
        return id;
    }

    public boolean isExpired() {
        return false;
    }

    public TicketGrantingTicket getGrantingTicket() {
        return parent;
    }

    public long getCreationTime() {
        return created.getTime();
    }

    public int getCountOfUses() {
        return 0;
    }
}
