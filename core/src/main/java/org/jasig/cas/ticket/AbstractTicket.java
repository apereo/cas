/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.util.Assert;

/**
 * Abstract implementation of a ticket that handles all ticket state for
 * policies. Also incorporates properties common among all tickets. As this is
 * an abstract class, it cannnot be instanciated. It is recommended that
 * implementations of the Ticket interface extend the AbstractTicket as it
 * handles common functionality amongst different ticket types (such as state
 * updating).
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public abstract class AbstractTicket implements Ticket, TicketState {

    /** The ExpirationPolicy this ticket will be following. */
    private final ExpirationPolicy expirationPolicy;

    /** The unique identifier for this ticket. */
    private final String id;

    /** The TicketGrantingTicket this is associated with. */
    private final TicketGrantingTicket ticketGrantingTicket;

    /** The last time this ticket was used. */
    private long lastTimeUsed;
    
    /** The previous last time this ticket was used. */
    private long previousLastTimeUsed;
    
    /** The time the ticket was created. */
    private long creationTime;

    /** The number of times this was used. */
    private int countOfUses;

    /**
     * Constructs a new Ticket with a unqiue id, a possible parent Ticket (can
     * be null) and a specified Expiration Policy.
     * 
     * @param id the unique identifier for the ticket
     * @param ticket the parent TicketGrantingTicket
     * @param expirationPolicy the expirartion policy for the ticket.
     * @throws IllegalArgumentException if the id or expiration policy is null.
     */
    public AbstractTicket(final String id, final TicketGrantingTicket ticket,
        final ExpirationPolicy expirationPolicy) {
        Assert.notNull(expirationPolicy, "expirationPolicy cannot be null");
        Assert.notNull(id, "id cannot be null");

        this.id = id;
        this.creationTime = System.currentTimeMillis();
        this.lastTimeUsed = System.currentTimeMillis();
        this.expirationPolicy = expirationPolicy;
        this.ticketGrantingTicket = ticket;
    }

    public final String getId() {
        return this.id;
    }

    protected final void updateState() {
        this.previousLastTimeUsed = this.lastTimeUsed;
        this.lastTimeUsed = System.currentTimeMillis();
        this.countOfUses++;
    }
    
    public final int getCountOfUses() {
        return this.countOfUses;
    }
    
    public final long getCreationTime() {
        return this.creationTime;
    }

    public final TicketGrantingTicket getGrantingTicket() {
        return this.ticketGrantingTicket;
    }
    
    public final long getLastTimeUsed() {
        return this.lastTimeUsed;
    }

    
    public final long getPreviousTimeUsed() {
        return this.previousLastTimeUsed;
    }

    public final boolean isExpired() {
        return this.expirationPolicy.isExpired(this) || isExpiredInternal();
    }

    protected abstract boolean isExpiredInternal();

    public final int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public final String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
