/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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

import org.jasig.cas.ticket.TicketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of an expiration policy that adds the concept of saying that a
 * ticket can only be used once every X milliseconds to prevent misconfigured
 * clients from consuming resources by doing constant redirects.
 *
 * @author Scott Battaglia
 * @since 3.0.0.5
 */
public final class ThrottledUseAndTimeoutExpirationPolicy extends AbstractCasExpirationPolicy {

    /** Serialization support. */
    private static final long serialVersionUID = 205979491183779408L;

    /**
     * The Logger instance for this class. Using a transient instance field for the Logger doesn't work, on object
     * deserialization the field is null.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ThrottledUseAndTimeoutExpirationPolicy.class);


    /** The time to kill in milliseconds. */
    private long timeToKillInMilliSeconds;

    /** Time time between which a ticket must wait to be used again. */
    private long timeInBetweenUsesInMilliSeconds;

    /** No-arg constructor used for serialization purposes. */
    public ThrottledUseAndTimeoutExpirationPolicy() {}

    public void setTimeInBetweenUsesInMilliSeconds(
        final long timeInBetweenUsesInMilliSeconds) {
        this.timeInBetweenUsesInMilliSeconds = timeInBetweenUsesInMilliSeconds;
    }

    public void setTimeToKillInMilliSeconds(final long timeToKillInMilliSeconds) {
        this.timeToKillInMilliSeconds = timeToKillInMilliSeconds;
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        final long currentTimeInMillis = System.currentTimeMillis();
        final long lastTimeTicketWasUsed = ticketState.getLastTimeUsed();

        if (ticketState.getCountOfUses() == 0
            && (currentTimeInMillis - lastTimeTicketWasUsed < this.timeToKillInMilliSeconds)) {
            LOGGER.debug("Ticket is not expired due to a count of zero and the time being less "
                    + "than the timeToKillInMilliseconds");
            return false;
        }

        if ((currentTimeInMillis - lastTimeTicketWasUsed >= this.timeToKillInMilliSeconds)) {
            LOGGER.debug("Ticket is expired due to the time being greater than the timeToKillInMilliseconds");
            return true;
        }

        if ((currentTimeInMillis - lastTimeTicketWasUsed <= this.timeInBetweenUsesInMilliSeconds)) {
            LOGGER.warn("Ticket is expired due to the time being less than the waiting period.");
            return true;
        }

        return false;
    }
}
