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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationBuilder;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.UniqueTicketIdGenerator;

/**
 * Mock ticket-granting ticket.
 *
 * @author Marvin S. Addison
 */
public class MockTicketGrantingTicket implements TicketGrantingTicket {

    private static final long serialVersionUID = 6546995681334670659L;

    public static final UniqueTicketIdGenerator ID_GENERATOR = new DefaultUniqueTicketIdGenerator();

    private final String id;

    private final Authentication authentication;

    private final Date created;

    private int usageCount;

    private boolean expired;


    public MockTicketGrantingTicket(final String principal) {
        id = ID_GENERATOR.getNewTicketId("TGT");
        authentication = new AuthenticationBuilder(new SimplePrincipal(principal)).build();
        created = new Date();
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public ServiceTicket grantServiceTicket(final Service service) {
        return grantServiceTicket(ID_GENERATOR.getNewTicketId("ST"), service, null, true);
    }

    public ServiceTicket grantServiceTicket(
            final String id,
            final Service service,
            final ExpirationPolicy expirationPolicy,
            final boolean credentialsProvided) {
        usageCount++;
        return new MockServiceTicket(id, service, this);
    }

    public boolean isRoot() {
        return true;
    }

    public TicketGrantingTicket getRoot() {
        return this;
    }

    public List<Authentication> getSupplementalAuthentications() {
        return Collections.emptyList();
    }

    public List<Authentication> getChainedAuthentications() {
        return Collections.emptyList();
    }

    public String getId() {
        return id;
    }

    public boolean isExpired() {
        return expired;
    }

    public TicketGrantingTicket getGrantingTicket() {
        return this;
    }

    public long getCreationTime() {
        return created.getTime();
    }

    public int getCountOfUses() {
        return usageCount;
    }

    @Override
    public Map<String, Service> getServices() {
        return Collections.emptyMap();
    }

    @Override
    public void removeAllServices() {
    }

    @Override
    public void markTicketExpired() {
        expired = true;
    }
}
