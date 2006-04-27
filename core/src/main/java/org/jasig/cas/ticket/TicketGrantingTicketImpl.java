/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.springframework.util.Assert;

/**
 * Concrete implementation of a TicketGrantingTicket. A TicketGrantingTicket is
 * the global identifier of a principal into the system. It grants the Principal
 * single-sign on access to any service that opts into single-sign on.
 * Expiration of a TicketGrantingTicket is controlled by the ExpirationPolicy
 * specified as object creation.
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

    /**
     * Constructs a new TicketGrantingTicket.
     * 
     * @param id the id of the Ticket
     * @param ticketGrantingTicket the parent ticket
     * @param authentication the Authentication request for this ticket
     * @param policy the expiration policy for this ticket.
     * @throws IllegalArgumentException if the Authentication object is null
     */
    public TicketGrantingTicketImpl(final String id,
        final TicketGrantingTicket ticketGrantingTicket,
        final Authentication authentication, final ExpirationPolicy policy) {
        super(id, ticketGrantingTicket, policy);

        Assert.notNull(authentication, "authentication cannot be null");

        this.authentication = authentication;
    }

    /**
     * Constructs a new TicketGrantingTicket without a parent
     * TicketGrantingTicket.
     * 
     * @param id the id of the Ticket
     * @param authentication the Authentication request for this ticket
     * @param policy the expiration policy for this ticket.
     */
    public TicketGrantingTicketImpl(final String id,
        final Authentication authentication, final ExpirationPolicy policy) {
        this(id, null, authentication, policy);
    }

    public Authentication getAuthentication() {
        return this.authentication;
    }

    public synchronized ServiceTicket grantServiceTicket(final String id,
        final Service service, final ExpirationPolicy expirationPolicy,
        final boolean credentialsProvided) {
        final ServiceTicket serviceTicket = new ServiceTicketImpl(id, this,
            service, this.getCountOfUses() == 0 || credentialsProvided,
            expirationPolicy);

        updateState();

        return serviceTicket;
    }

    public boolean isRoot() {
        return this.getGrantingTicket() == null;
    }

    public void expire() {
        this.expired = true;
    }

    public boolean isExpiredInternal() {
        return this.expired
            || (this.getGrantingTicket() != null && this.getGrantingTicket()
                .isExpired());
    }

    public List getChainedAuthentications() {
        final List list = new ArrayList();

        if (this.getGrantingTicket() == null) {
            list.add(this.getAuthentication());
            return Collections.unmodifiableList(list);
        }

        list.add(this.getAuthentication());
        list.addAll(this.getGrantingTicket().getChainedAuthentications());

        return Collections.unmodifiableList(list);
    }

    public boolean equals(final Object object) {
        if (object == null
            || !TicketGrantingTicket.class.isAssignableFrom(object.getClass())) {
            return false;
        }

        final TicketGrantingTicket ticketGrantingTicket = (TicketGrantingTicket) object;

        return new EqualsBuilder().append(
            ticketGrantingTicket.getAuthentication(), this.getAuthentication())
            .append(ticketGrantingTicket.getChainedAuthentications(),
                this.getChainedAuthentications()).append(
                ticketGrantingTicket.getCreationTime(), this.getCreationTime())
            .append(ticketGrantingTicket.getGrantingTicket(),
                this.getGrantingTicket()).append(ticketGrantingTicket.getId(),
                this.getId()).append(ticketGrantingTicket.isExpired(),
                this.isExpired()).append(ticketGrantingTicket.isRoot(),
                this.isRoot()).isEquals();
    }
}
