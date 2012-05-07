/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.ticket.support;

import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.TicketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of an expiration policy that adds the concept of saying that a
 * ticket can only be used once every X millseconds to prevent misconfigured
 * clients from consuming resources by doing constant redirects.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.5
 */
public final class ThrottledUseAndTimeoutExpirationPolicy implements
    ExpirationPolicy {

    private static final Logger log = LoggerFactory.getLogger(ThrottledUseAndTimeoutExpirationPolicy.class);

    /** Static ID for serialization. */
    private static final long serialVersionUID = -848036845536731268L;

    /** The time to kill in milliseconds. */
    private long timeToKillInMilliSeconds;

    /** Time time between which a ticket must wait to be used again. */
    private long timeInBetweenUsesInMilliSeconds;

    public void setTimeInBetweenUsesInMilliSeconds(
        final long timeInBetweenUsesInMilliSeconds) {
        this.timeInBetweenUsesInMilliSeconds = timeInBetweenUsesInMilliSeconds;
    }

    public void setTimeToKillInMilliSeconds(final long timeToKillInMilliSeconds) {
        this.timeToKillInMilliSeconds = timeToKillInMilliSeconds;
    }

    public boolean isExpired(final TicketState ticketState) {
        if (ticketState.getCountOfUses() == 0
            && (System.currentTimeMillis() - ticketState.getLastTimeUsed() < this.timeToKillInMilliSeconds)) {
            if (log.isDebugEnabled()) {
                log
                    .debug("Ticket is not expired due to a count of zero and the time being less than the timeToKillInMilliseconds");
            }
            return false;
        }

        if ((System.currentTimeMillis() - ticketState.getLastTimeUsed() >= this.timeToKillInMilliSeconds)) {
            if (log.isDebugEnabled()) {
                log
                    .debug("Ticket is expired due to the time being greater than the timeToKillInMilliseconds");
            }
            return true;
        }

        if ((System.currentTimeMillis() - ticketState.getLastTimeUsed() <= this.timeInBetweenUsesInMilliSeconds)) {
            log
                .warn("Ticket is expired due to the time being less than the waiting period.");
            return true;
        }

        return false;
    }
}
