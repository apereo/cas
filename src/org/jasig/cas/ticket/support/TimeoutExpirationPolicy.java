/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.support;

import org.jasig.cas.ticket.AbstractTicket;
import org.jasig.cas.ticket.ExpirationPolicy;

/**
 * Expiration policy that is based on a certain time period for a ticket to exist.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class TimeoutExpirationPolicy implements ExpirationPolicy {

    final private long timeToKillInMilliSeconds;

    public TimeoutExpirationPolicy(final long timeToKillInMilliSeconds) {
        this.timeToKillInMilliSeconds = timeToKillInMilliSeconds;
    }

    /**
     * @see org.jasig.cas.ticket.ExpirationPolicy#isExpired(org.jasig.cas.ticket.AbstractTicket)
     */
    public boolean isExpired(final AbstractTicket ticket) {
        return System.currentTimeMillis() - ticket.getLastUsedTime() > this.timeToKillInMilliSeconds;
    }
}