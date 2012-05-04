/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
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
 * @version $Revision: $
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
