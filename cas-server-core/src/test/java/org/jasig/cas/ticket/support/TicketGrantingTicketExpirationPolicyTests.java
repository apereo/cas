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

import static org.junit.Assert.*;
import org.jasig.cas.TestUtils;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.junit.Before;
import org.junit.Test;

/**
 * @author William G. Thompson, Jr.

 * @since 3.4.10
 */
public class TicketGrantingTicketExpirationPolicyTests {

    private static final long HARD_TIMEOUT = 100L;

    private static final long SLIDING_TIMEOUT = 60L; 

    private static final long TIMEOUT_BUFFER = 20L; // needs to be long enough for timeouts to be triggered

    private TicketGrantingTicketExpirationPolicy expirationPolicy;

    private TicketGrantingTicket ticketGrantingTicket;

    @Before
    public void setUp() throws Exception {
        this.expirationPolicy = new TicketGrantingTicketExpirationPolicy();
        this.expirationPolicy.setMaxTimeToLiveInMilliSeconds(HARD_TIMEOUT);
        this.expirationPolicy.setTimeToKillInMilliSeconds(SLIDING_TIMEOUT);
        this.ticketGrantingTicket = new TicketGrantingTicketImpl("test", TestUtils.getAuthentication(),
                this.expirationPolicy);
    }

    @Test
    public void verifyTgtIsExpiredByHardTimeOut() throws InterruptedException {
         // keep tgt alive via sliding window until within SLIDING_TIME / 2 of the HARD_TIMEOUT
         while (System.currentTimeMillis() - ticketGrantingTicket.getCreationTime()
                 < (HARD_TIMEOUT - SLIDING_TIMEOUT / 2)) {
             ticketGrantingTicket.grantServiceTicket("test", TestUtils.getService(), expirationPolicy, false);
             Thread.sleep(SLIDING_TIMEOUT - TIMEOUT_BUFFER);
             assertFalse(this.ticketGrantingTicket.isExpired());
         }

         // final sliding window extension past the HARD_TIMEOUT
         ticketGrantingTicket.grantServiceTicket("test", TestUtils.getService(), expirationPolicy, false);
         Thread.sleep(SLIDING_TIMEOUT / 2 + TIMEOUT_BUFFER);
         assertTrue(ticketGrantingTicket.isExpired());

    }

    @Test
    public void verifyTgtIsExpiredBySlidingWindow() throws InterruptedException {
        ticketGrantingTicket.grantServiceTicket("test", TestUtils.getService(), expirationPolicy, false);
        Thread.sleep(SLIDING_TIMEOUT - TIMEOUT_BUFFER);
        assertFalse(ticketGrantingTicket.isExpired());

        ticketGrantingTicket.grantServiceTicket("test", TestUtils.getService(), expirationPolicy, false);
        Thread.sleep(SLIDING_TIMEOUT - TIMEOUT_BUFFER);
        assertFalse(ticketGrantingTicket.isExpired());

        ticketGrantingTicket.grantServiceTicket("test", TestUtils.getService(), expirationPolicy, false);
        Thread.sleep(SLIDING_TIMEOUT + TIMEOUT_BUFFER);
        assertTrue(ticketGrantingTicket.isExpired());

    }

}
