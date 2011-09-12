/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.ticket.support;

import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.TicketState;

/**
 * Expiration policy that is based on a certain time period for a ticket to
 * exist.
 * <p>
 * The expiration policy defined by this class is one of inactivity.  If you are inactive for the specified
 * amount of time, the ticket will be expired.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class TimeoutExpirationPolicy implements ExpirationPolicy {

    /** Serializable ID. */
    private static final long serialVersionUID = 3545511790222979383L;

    /** The time to kill in milliseconds. */
    private final long timeToKillInMilliSeconds;

    public TimeoutExpirationPolicy(final long timeToKillInMilliSeconds) {
        this.timeToKillInMilliSeconds = timeToKillInMilliSeconds;
    }

    public boolean isExpired(final TicketState ticketState) {
        return (ticketState == null)
            || (System.currentTimeMillis() - ticketState.getLastTimeUsed() >= this.timeToKillInMilliSeconds);
    }
}
