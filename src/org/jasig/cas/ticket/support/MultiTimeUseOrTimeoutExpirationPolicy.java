/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.support;

import org.jasig.cas.ticket.AbstractTicket;
import org.jasig.cas.ticket.ExpirationPolicy;

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

    /**
     * 
     * @see org.jasig.cas.ticket.ExpirationPolicy#isExpired(org.jasig.cas.ticket.AbstractTicket)
     */
    public boolean isExpired(final AbstractTicket ticket) {
        return (ticket.getCountOfUses() > numberOfUses || System.currentTimeMillis() - ticket.getLastUsedTime() > timeToKillInMilliSeconds);
    }
}