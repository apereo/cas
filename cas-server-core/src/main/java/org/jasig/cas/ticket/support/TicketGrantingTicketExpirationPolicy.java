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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;

/**
 * Provides the Ticket Granting Ticket expiration policy.  Ticket Granting Tickets
 * can be used any number of times, have a fixed lifetime, and an idle timeout.
 *
 * @author William G. Thompson, Jr.
 * @since 3.4.10
 */
public final class TicketGrantingTicketExpirationPolicy extends AbstractCasExpirationPolicy implements InitializingBean {

    /**
     * The Logger instance for this class. Using a transient instance field for the Logger doesn't work, on object
     * deserialization the field is null.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TicketGrantingTicketExpirationPolicy.class);

    /** Serialization support. */
    private static final long serialVersionUID = 7670537200691354820L;

    /** Maximum time this ticket is valid.  */
    private long maxTimeToLiveInMilliSeconds;

    /** Time to kill in milliseconds. */
    private long timeToKillInMilliSeconds;

    /**
     * @deprecated As of 4.1.
     * Instantiates a new Ticket granting ticket expiration policy.
     */
    @Deprecated
    public TicketGrantingTicketExpirationPolicy() {
        this.maxTimeToLiveInMilliSeconds = 0;
        this.timeToKillInMilliSeconds = 0;
    }

    /**
     * Instantiates a new Ticket granting ticket expiration policy.
     *
     * @param maxTimeToLive the max time to live
     * @param timeToKill the time to kill
     * @param timeUnit the time unit
     */
    public TicketGrantingTicketExpirationPolicy(final long maxTimeToLive, final long timeToKill, final TimeUnit timeUnit) {
        this.maxTimeToLiveInMilliSeconds = timeUnit.toMillis(maxTimeToLive);
        this.timeToKillInMilliSeconds = timeUnit.toMillis(timeToKill);
    }

    /**
     * @deprecated As of 4.1.
     * Set max time to live in milli seconds.
     *
     * @param maxTimeToLiveInMilliSeconds the max time to live in milli seconds
     */
    @Deprecated
    public void setMaxTimeToLiveInMilliSeconds(final long maxTimeToLiveInMilliSeconds){
        this.maxTimeToLiveInMilliSeconds = maxTimeToLiveInMilliSeconds;
    }

    /**
     * @deprecated As of 4.1.
     * Sets time to kill in milli seconds.
     *
     * @param timeToKillInMilliSeconds the time to kill in milli seconds
     */
    @Deprecated
    public void setTimeToKillInMilliSeconds(final long timeToKillInMilliSeconds) {
        this.timeToKillInMilliSeconds = timeToKillInMilliSeconds;
    }

    /**
     * @deprecated As of 4.1.
     * Convenient virtual property setter to set time in seconds.
     * @param maxTimeToLiveInSeconds max number of seconds for the tickets to stay alive
     **/
    @Deprecated
    public void setMaxTimeToLiveInSeconds(final long maxTimeToLiveInSeconds){
        if(this.maxTimeToLiveInMilliSeconds == 0L) {
            this.maxTimeToLiveInMilliSeconds = TimeUnit.SECONDS.toMillis(maxTimeToLiveInSeconds);
        }
    }

    /**
     * @deprecated As of 4.1.
     * @param timeToKillInSeconds time for the ticket to stay active in seconds
     * Convenient virtual property setter to set time in seconds.
     **/
    @Deprecated
    public void setTimeToKillInSeconds(final long timeToKillInSeconds) {
        if(this.timeToKillInMilliSeconds == 0L) {
            this.timeToKillInMilliSeconds = TimeUnit.SECONDS.toMillis(timeToKillInSeconds);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.isTrue((maxTimeToLiveInMilliSeconds >= timeToKillInMilliSeconds),
                "maxTimeToLiveInMilliSeconds must be greater than or equal to timeToKillInMilliSeconds.");
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        final long currentSystemTimeInMillis = System.currentTimeMillis();

        // Ticket has been used, check maxTimeToLive (hard window)
        if ((currentSystemTimeInMillis - ticketState.getCreationTime() >= maxTimeToLiveInMilliSeconds)) {
            LOGGER.debug("Ticket is expired because the time since creation is greater than maxTimeToLiveInMilliSeconds");
            return true;
        }

        // Ticket is within hard window, check timeToKill (sliding window)
        if ((currentSystemTimeInMillis - ticketState.getLastTimeUsed() >= timeToKillInMilliSeconds)) {
            LOGGER.debug("Ticket is expired because the time since last use is greater than timeToKillInMilliseconds");
            return true;
        }

        return false;
    }

}
