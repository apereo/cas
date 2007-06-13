/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.ticket.support;

import org.jasig.cas.TestUtils;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class ThrottledUseAndTimeoutExpirationPolicyTests extends TestCase {

    private static final long TIMEOUT = 5000;

    private ThrottledUseAndTimeoutExpirationPolicy expirationPolicy;

    private TicketGrantingTicket ticket;

    protected void setUp() throws Exception {
        this.expirationPolicy = new ThrottledUseAndTimeoutExpirationPolicy();
        this.expirationPolicy.setTimeToKillInMilliSeconds(TIMEOUT);
        this.expirationPolicy.setTimeInBetweenUsesInMilliSeconds(1000);

        this.ticket = new TicketGrantingTicketImpl("test", TestUtils
            .getAuthentication(), this.expirationPolicy);

        super.setUp();
    }

    public void testTicketIsNotExpired() {
        assertFalse(this.ticket.isExpired());
    }
    
    public void testTicketIsExpired() {
        try {
            Thread.sleep(TIMEOUT + 100);
            assertTrue(this.ticket.isExpired());
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }
    
    public void testTicketUsedButWithTimeout() {
        try {
            this.ticket.grantServiceTicket("test", TestUtils.getService(), this.expirationPolicy, false);
            Thread.sleep(TIMEOUT -100);
            assertFalse(this.ticket.isExpired());
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }
    
    public void testNotWaitingEnoughTime() {
        this.ticket.grantServiceTicket("test", TestUtils.getService(), this.expirationPolicy, false);
        assertTrue(this.ticket.isExpired());
    }
}
