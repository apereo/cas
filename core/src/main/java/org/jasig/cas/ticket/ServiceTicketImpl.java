/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.Service;
import org.jasig.cas.util.UniqueTicketIdGenerator;

/**
 * Domain object representing a Service Ticket. A service ticket grants specific
 * access to a particular service. It will only work for a particular service.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class ServiceTicketImpl extends AbstractTicket implements ServiceTicket {

    private static final long serialVersionUID = 1296808733190507408L;

    private final Service service;

    private boolean fromNewLogin;

    private final UniqueTicketIdGenerator uniqueTicketIdGenerator;

    private final ExpirationPolicy ticketGrantingTicketExpirationPolicy;

    private final ExpirationPolicy expirationPolicy;

    public ServiceTicketImpl(final String id,
        final TicketGrantingTicket ticket, final Service service,
        final boolean fromNewLogin, final ExpirationPolicy policy,
        final UniqueTicketIdGenerator uniqueTicketIdGenerator,
        final ExpirationPolicy ticketGrantingTicketExpirationPolicy) {
        super(id, ticket, policy);

        if (ticket == null || service == null)
            throw new IllegalArgumentException(
                "ticket and service are required parameters");

        this.service = service;
        this.fromNewLogin = fromNewLogin;
        this.uniqueTicketIdGenerator = uniqueTicketIdGenerator;
        this.ticketGrantingTicketExpirationPolicy = ticketGrantingTicketExpirationPolicy;
        this.expirationPolicy = policy;
    }

    public boolean isFromNewLogin() {
        return this.fromNewLogin;
    }

    public void setFromNewLogin(boolean fromNewLogin) {
        this.fromNewLogin = fromNewLogin;
    }

    public Service getService() {
        return this.service;
    }

    public boolean isExpired() {
        return super.isExpired() || this.getGrantingTicket().isExpired();
    }

    public TicketGrantingTicket grantTicketGrantingTicket(
        final Authentication authentication) {
        return new TicketGrantingTicketImpl(this.uniqueTicketIdGenerator
            .getNewTicketId(TicketGrantingTicket.PREFIX), this
            .getGrantingTicket(), authentication,
            this.ticketGrantingTicketExpirationPolicy,
            this.uniqueTicketIdGenerator, this.expirationPolicy);
    }

}