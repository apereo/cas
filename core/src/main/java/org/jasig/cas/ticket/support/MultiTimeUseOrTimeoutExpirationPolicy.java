/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.support;

import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.Ticket;

/**
 * ExpirationPolicy that is based on certain number of uses of a ticket or a certain time period for a ticket to exist.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class MultiTimeUseOrTimeoutExpirationPolicy implements ExpirationPolicy {

    final private long timeToKillInMilliSeconds;

    final private int numberOfUses;

    public MultiTimeUseOrTimeoutExpirationPolicy(final int numberOfUses, final long timeToKillInMilliSeconds) {
        this.timeToKillInMilliSeconds = timeToKillInMilliSeconds;
        this.numberOfUses = numberOfUses;
    }

    public boolean isExpired(final Ticket ticket) {
        return (ticket == null) || (ticket.getCountOfUses() > this.numberOfUses) || (System.currentTimeMillis() - ticket.getLastTimeUsed() >= this.timeToKillInMilliSeconds);
    }
}