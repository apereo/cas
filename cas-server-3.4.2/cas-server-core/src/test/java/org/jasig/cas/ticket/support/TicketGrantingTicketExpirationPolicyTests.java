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
 */
public class TicketGrantingTicketExpirationPolicyTests extends TestCase {

    private static final long HARD_TIMEOUT = 10000; // 10s

    private static final long SLIDING_TIMEOUT = 2000; // 2s

    private static final long COOL_DOWN = 500; // 1/2s

    private TicketGrantingTicketExpirationPolicy expirationPolicy;

    private TicketGrantingTicket ticketGrantingTicket;

    protected void setUp() throws Exception {
        expirationPolicy = new TicketGrantingTicketExpirationPolicy();
        expirationPolicy.setMaxTimeToLiveInMilliSeconds(HARD_TIMEOUT);
        expirationPolicy.setTimeToKillInMilliSeconds(SLIDING_TIMEOUT);
        expirationPolicy.setMinTimeInBetweenUsesInMilliSeconds(COOL_DOWN);
        ticketGrantingTicket = new TicketGrantingTicketImpl("test", TestUtils.getAuthentication(), expirationPolicy);
        super.setUp();
    }

     public void testTgtIsExpiredByHardTimeOut() {
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
             fail(e.getMessage());
         }
     }

    public void testTgtIsExpiredBySlidingWindow() {
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
            fail(e.getMessage());
        }
    }

    // XXX problem with CoolDownTime...if RegistryCleaner calls isExpired within CoolDown period it will remove a valid ticket!?!?
    public void testTgtUsedBeforeCoolDownTime() {
        try {
            assertFalse(ticketGrantingTicket.isExpired());
            ticketGrantingTicket.grantServiceTicket("test", TestUtils.getService(), expirationPolicy, false);
            assertTrue(ticketGrantingTicket.isExpired());  // could be called by CASImpl or RegistryCleaner
            Thread.sleep(COOL_DOWN + 100);
            assertFalse(ticketGrantingTicket.isExpired()); // would have been deleted by CASImpl or RegistryCleaner
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }
}
