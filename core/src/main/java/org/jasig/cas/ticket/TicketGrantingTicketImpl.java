/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.Service;

/**
 * Domain object to model a ticket granting ticket.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class TicketGrantingTicketImpl extends AbstractTicket implements
    TicketGrantingTicket {

    /** The Unique ID for serializing. */
    private static final long serialVersionUID = -8673232562725683059L;

    /** The authenticated object for which this ticket was generated for. */
    private final Authentication authentication;

    /** Flag to enforce manual expiration. */
    private boolean expired = false;

    public TicketGrantingTicketImpl(final String id,
        final TicketGrantingTicket ticketGrantingTicket,
        final Authentication authentication, final ExpirationPolicy policy) {
        super(id, ticketGrantingTicket, policy);

        if (authentication == null) {
            throw new IllegalArgumentException(
                "authentication cannot be null on "
                    + this.getClass().getName());
        }

        this.authentication = authentication;
    }

    public TicketGrantingTicketImpl(final String id,
        final Authentication authentication, final ExpirationPolicy policy) {
        this(id, null, authentication, policy);
    }

    public Authentication getAuthentication() {
        return this.authentication;
    }

    public synchronized ServiceTicket grantServiceTicket(final String id, final Service service, final ExpirationPolicy expirationPolicy) {
        final ServiceTicket serviceTicket = new ServiceTicketImpl(
            id,
            this, service, this.getCountOfUses() == 0,
            expirationPolicy);

        this.incrementCountOfUses();

        return serviceTicket;
    }

    public boolean isRoot() {
        return this.getGrantingTicket() == null;
    }

    public void expire() {
        this.expired = true;
    }

    public boolean isExpired() {
        return super.isExpired()
            || this.expired
            || (this.getGrantingTicket() != null && this.getGrantingTicket()
                .isExpired());
    }

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
