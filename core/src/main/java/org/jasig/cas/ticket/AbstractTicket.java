/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Abstract implementation of a ticket that handles all ticket state for
 * policies. Also incorporates properties common among all tickets.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public abstract class AbstractTicket implements Ticket {

    final private ExpirationPolicy expirationPolicy;

    private long lastTimeUsed;

    private int countOfUses;

    final private String id;

    final private TicketGrantingTicket ticketGrantingTicket;

    public AbstractTicket(final String id, TicketGrantingTicket ticket,
        final ExpirationPolicy expirationPolicy) {
        if (expirationPolicy == null || id == null)
            throw new IllegalArgumentException(
                "id and expirationPolicy are required parameters.");

        this.id = id;
        this.lastTimeUsed = System.currentTimeMillis();

        this.expirationPolicy = expirationPolicy;

        this.ticketGrantingTicket = ticket;
    }

    public final String getId() {
        return this.id;
    }

    public final int getCountOfUses() {
        return this.countOfUses;
    }

    public final long getLastTimeUsed() {
        return this.lastTimeUsed;
    }

    public final void incrementCountOfUses() {
        this.countOfUses++;
    }

    public final void updateLastTimeUsed() {
        this.lastTimeUsed = System.currentTimeMillis();
    }

    /**
     * @return Returns the ticketGrantingTicket.
     */
    public final TicketGrantingTicket getGrantingTicket() {
        return this.ticketGrantingTicket;
    }

    public boolean isExpired() {
        return this.expirationPolicy.isExpired(this);
    }

    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
