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

import java.util.concurrent.TimeUnit;

/**
 * Expiration policy that is based on a certain time period for a ticket to
 * exist.
 * <p>
 * The expiration policy defined by this class is one of inactivity.  If you are inactive for the specified
 * amount of time, the ticket will be expired.
 *
 * @author Scott Battaglia

 * @since 3.0.0
 */
public final class TimeoutExpirationPolicy extends AbstractCasExpirationPolicy {

    /** Serialization support. */
    private static final long serialVersionUID = -7636642464326939536L;

    /** The time to kill in milliseconds. */
    private final long timeToKillInMilliSeconds;

    /** No-arg constructor for serialization support. */
    private TimeoutExpirationPolicy() {
        this.timeToKillInMilliSeconds = 0;
    }

    /**
     * Instantiates a new timeout expiration policy.
     *
     * @param timeToKillInMilliSeconds the time to kill in milli seconds
     */
    public TimeoutExpirationPolicy(final long timeToKillInMilliSeconds) {
        this.timeToKillInMilliSeconds = timeToKillInMilliSeconds;
    }

    /**
     * Instantiates a new Timeout expiration policy.
     *
     * @param timeToKill the time to kill
     * @param timeUnit the time unit
     */
    public TimeoutExpirationPolicy(final long timeToKill, final TimeUnit timeUnit) {
        this.timeToKillInMilliSeconds = timeUnit.toMillis(timeToKill);
    }

    @Override
    public boolean isExpired(final TicketState ticketState) {
        return (ticketState == null)
            || (System.currentTimeMillis() - ticketState.getLastTimeUsed() >= this.timeToKillInMilliSeconds);
    }
}
