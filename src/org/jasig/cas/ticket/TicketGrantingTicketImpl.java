/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.Service;
import org.jasig.cas.util.UniqueTicketIdGenerator;

/**
 * Domain object to model a ticket granting ticket.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class TicketGrantingTicketImpl extends AbstractTicket implements TicketGrantingTicket {

    private static final long serialVersionUID = -8673232562725683059L;

    private final Authentication authentication;

    private final UniqueTicketIdGenerator uniqueTicketIdGenerator;

    private final ExpirationPolicy serviceExpirationPolicy;

    private final ExpirationPolicy expirationPolicy;

    private boolean expired = false;

    public TicketGrantingTicketImpl(final String id, final TicketGrantingTicket ticketGrantingTicket, final Authentication authentication,
        final ExpirationPolicy policy, final UniqueTicketIdGenerator uniqueTicketIdGenerator, final ExpirationPolicy serviceExpirationPolicy) {
        super(id, ticketGrantingTicket, policy);

        if (authentication == null || uniqueTicketIdGenerator == null || serviceExpirationPolicy == null) {
            throw new IllegalArgumentException("authentication, uniqueTicketIdGenerator, and serviceExpirationPolicy cannot be null on "
                + this.getClass().getName());
        }

        this.authentication = authentication;
        this.uniqueTicketIdGenerator = uniqueTicketIdGenerator;
        this.serviceExpirationPolicy = serviceExpirationPolicy;
        this.expirationPolicy = policy;
    }

    public TicketGrantingTicketImpl(final String id, final Authentication authentication, final ExpirationPolicy policy,
        final UniqueTicketIdGenerator uniqueTicketIdGenerator, final ExpirationPolicy serviceExpirationPolicy) {
        this(id, null, authentication, policy, uniqueTicketIdGenerator, serviceExpirationPolicy);
    }

    /**
     * @return Returns the authentication object.
     */
    public Authentication getAuthentication() {
        return this.authentication;
    }

    /**
     * @see org.jasig.cas.ticket.InternalTicketGrantingTicket#grantServiceTicket(org.jasig.cas.Service)
     */
    public synchronized ServiceTicket grantServiceTicket(Service service) {
        final ServiceTicket serviceTicket = new ServiceTicketImpl(this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX), this, service,
            this.getCountOfUses() == 0, this.serviceExpirationPolicy, this.uniqueTicketIdGenerator, this.expirationPolicy);
        
        this.incrementCountOfUses();

        return serviceTicket;
    }

    /**
     * @see org.jasig.cas.ticket.TicketGrantingTicket#isRoot()
     */
    public boolean isRoot() {
        return this.getGrantingTicket() == null;
    }

    /**
     * @see org.jasig.cas.ticket.TicketGrantingTicket#expire()
     */
    public void expire() {
        this.expired = true;
    }

    /**
     * @see org.jasig.cas.ticket.Ticket#isExpired()
     */
    public boolean isExpired() {
        return super.isExpired() || this.expired;
    }

    /**
     * @see org.jasig.cas.ticket.TicketGrantingTicket#getChainedPrincipals()
     */
    public List getChainedPrincipals() {
        final List list = new ArrayList();

        if (this.getGrantingTicket() == null) {
            list.add(this.getAuthentication().getPrincipal());
            return Collections.unmodifiableList(list);
        }

        list.addAll(this.getGrantingTicket().getChainedPrincipals());
        list.add(this.getAuthentication().getPrincipal());

        return Collections.unmodifiableList(list);
    }
}