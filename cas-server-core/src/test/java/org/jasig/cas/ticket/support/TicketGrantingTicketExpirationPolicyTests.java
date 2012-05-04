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

import junit.framework.TestCase;
import org.jasig.cas.TestUtils;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;

/**
 * @author William G. Thompson, Jr.
 * @version $Revision$ $Date$
 * @since 3.4.10
 */
public class TicketGrantingTicketExpirationPolicyTests extends TestCase {

    private static final long HARD_TIMEOUT = 10000L; // 10s

    private static final long SLIDING_TIMEOUT = 2000L; // 2s

    private static final long HARD_TIMEOUT_SECONDS = 10L;

    private static final long SLIDING_TIMEOUT_SECONDS = 2L;

    private TicketGrantingTicketExpirationPolicy expirationPolicy;

    private TicketGrantingTicket ticketGrantingTicket;

    protected void setUp() throws Exception {
        this.expirationPolicy = new TicketGrantingTicketExpirationPolicy();
        this.expirationPolicy.setMaxTimeToLiveInSeconds(HARD_TIMEOUT_SECONDS);
        this.expirationPolicy.setTimeToKillInSeconds(SLIDING_TIMEOUT_SECONDS);
        this.ticketGrantingTicket = new TicketGrantingTicketImpl("test", TestUtils.getAuthentication(), this.expirationPolicy);
    }

    public void testTgtIsExpiredByHardTimeOut() throws InterruptedException {
     try {
         // keep tgt alive via sliding window until within SLIDING_TIME / 2 of the HARD_TIMEOUT
         while (System.currentTimeMillis() - ticketGrantingTicket.getCreationTime() < (HARD_TIMEOUT - SLIDING_TIMEOUT / 2)) {
             ticketGrantingTicket.grantServiceTicket("test", TestUtils.getService(), expirationPolicy, false);
             Thread.sleep(SLIDING_TIMEOUT - 100);
             assertFalse(this.ticketGrantingTicket.isExpired());
         }

         // final sliding window extension past the HARD_TIMEOUT
         ticketGrantingTicket.grantServiceTicket("test", TestUtils.getService(), expirationPolicy, false);
         Thread.sleep(SLIDING_TIMEOUT / 2 + 100);
         assertTrue(ticketGrantingTicket.isExpired());

     } catch (InterruptedException e) {
         throw e;
     }
    }

    public void testTgtIsExpiredBySlidingWindow() throws InterruptedException {
        try {
            ticketGrantingTicket.grantServiceTicket("test", TestUtils.getService(), expirationPolicy, false);
            Thread.sleep(SLIDING_TIMEOUT - 100);
            assertFalse(ticketGrantingTicket.isExpired());

            ticketGrantingTicket.grantServiceTicket("test", TestUtils.getService(), expirationPolicy, false);
            Thread.sleep(SLIDING_TIMEOUT - 100);
            assertFalse(ticketGrantingTicket.isExpired());

            ticketGrantingTicket.grantServiceTicket("test", TestUtils.getService(), expirationPolicy, false);
            Thread.sleep(SLIDING_TIMEOUT + 100);
            assertTrue(ticketGrantingTicket.isExpired());

        } catch (InterruptedException e) {
            throw e;
        }
    }

}
