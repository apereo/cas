/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
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

    private static final long HARD_TIMEOUT = 10000; // 10s

    private static final long SLIDING_TIMEOUT = 2000; // 2s

    private TicketGrantingTicketExpirationPolicy expirationPolicy;

    private TicketGrantingTicket ticketGrantingTicket;

    protected void setUp() throws Exception {
        expirationPolicy = new TicketGrantingTicketExpirationPolicy();
        expirationPolicy.setMaxTimeToLiveInMilliSeconds(HARD_TIMEOUT);
        expirationPolicy.setTimeToKillInMilliSeconds(SLIDING_TIMEOUT);
        ticketGrantingTicket = new TicketGrantingTicketImpl("test", TestUtils.getAuthentication(), expirationPolicy);
        super.setUp();
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
