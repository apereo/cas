/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */
package org.jasig.cas.mock;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.ImmutableAuthentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.UniqueTicketIdGenerator;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Mock ticket-granting ticket;
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public class MockTicketGrantingTicket implements TicketGrantingTicket {

    public static final UniqueTicketIdGenerator ID_GENERATOR = new DefaultUniqueTicketIdGenerator();
    
    private final String id;
    
    private final Authentication authentication;
    
    private final Date created;
    
    private int usageCount;

    private boolean expired;
            

    public MockTicketGrantingTicket(final String principal) {
        id = ID_GENERATOR.getNewTicketId("TGT");
        authentication = new ImmutableAuthentication(new SimplePrincipal(principal));
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

    public void expire() {
        expired = true;
    }

    public boolean isRoot() {
        return true;
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
}
