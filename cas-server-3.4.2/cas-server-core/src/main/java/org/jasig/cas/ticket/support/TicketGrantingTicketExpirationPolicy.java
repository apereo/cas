/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.ticket.support;

import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.TicketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the Ticket Granting Ticket expiration policy.  Ticket Granting Tickets
 * can be used any number of times, have a fixed lifetime, and an idle timeout.
 *
 * @author William G. Thompson, Jr.
 * @version $Revision$ $Date$
 */
public final class TicketGrantingTicketExpirationPolicy implements ExpirationPolicy {

    private static final Logger log = LoggerFactory.getLogger(TicketGrantingTicketExpirationPolicy.class);

    /** Static ID for serialization. */
    private static final long serialVersionUID = 2136490343650084287L;

    /** Maximum time this ticket is valid  */
    private long maxTimeToLiveInMilliSeconds;

    /** Time to kill in milliseconds. */
    private long timeToKillInMilliSeconds;

    /** Time between which a ticket must wait to be used again. */
    private long timeInBetweenUsesInMilliSeconds;

    public void setMaxTimeToLiveInMilliSeconds(final long maxTimeToLiveInMilliSeconds){
        this.maxTimeToLiveInMilliSeconds = maxTimeToLiveInMilliSeconds;
    }

    public void setTimeToKillInMilliSeconds(final long timeToKillInMilliSeconds) {
        this.timeToKillInMilliSeconds = timeToKillInMilliSeconds;
    }

    public void setTimeInBetweenUsesInMilliSeconds(final long timeInBetweenUsesInMilliSeconds) {
        this.timeInBetweenUsesInMilliSeconds = timeInBetweenUsesInMilliSeconds;
    }

    public boolean isExpired(final TicketState ticketState) {

        // Ticket hasn't been used yet, check expiration timeout
        if (ticketState.getCountOfUses() == 0) {
            if ((System.currentTimeMillis() - ticketState.getCreationTime() < timeToKillInMilliSeconds)) {
                if (log.isDebugEnabled()) {
                    log.debug("Ticket is not expired due to a uses count of zero and the time being within timeToKillInMilliseconds");
                }
                return false;
            }
        }

        // Ticket has been used, check maxTimeToLive (hard window)
        if ((System.currentTimeMillis() - ticketState.getCreationTime() >= maxTimeToLiveInMilliSeconds)) {
            if (log.isDebugEnabled()) {
                log.debug("Ticket is expired due to the time being greater than the maxTimeToLiveInMilliSeconds");
            }
            return true;
        }

        // Ticket is within hard window, check timeToKill (sliding window)
        if ((System.currentTimeMillis() - ticketState.getLastTimeUsed() >= timeToKillInMilliSeconds)) {
            if (log.isDebugEnabled()) {
                log.debug("Ticket is expired due to the time being greater than the timeToKillInMilliseconds");
            }
            return true;
        }

        // Ticket is within timeouts, check cool down period.
        if ((System.currentTimeMillis() - ticketState.getLastTimeUsed() <= timeInBetweenUsesInMilliSeconds)) {
            log.warn("Ticket is expired due to the time being less than the waiting period.");
            return true;
        }

        return false;
    }
}
