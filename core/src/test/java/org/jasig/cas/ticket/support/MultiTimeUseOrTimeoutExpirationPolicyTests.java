/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.support;

import org.jasig.cas.TestUtils;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class MultiTimeUseOrTimeoutExpirationPolicyTests extends TestCase {

    private static final long TIMEOUT = 5000;

    private static final int NUMBER_OF_USES = 5;

    private ExpirationPolicy expirationPolicy;

    private Ticket ticket;

    protected void setUp() throws Exception {
        this.expirationPolicy = new MultiTimeUseOrTimeoutExpirationPolicy(
            NUMBER_OF_USES, TIMEOUT);

        this.ticket = new TicketGrantingTicketImpl("test",
            TestUtils.getAuthentication(), this.expirationPolicy);

        super.setUp();
    }

    public void testTicketIsNull() {
        assertTrue(this.expirationPolicy.isExpired(null));
    }

    public void testTicketIsNotExpired() {
        assertFalse(this.ticket.isExpired());
    }

    public void testTicketIsExpiredByTime() {
        try {
            Thread.sleep(TIMEOUT + 15);
            assertTrue(this.ticket.isExpired());
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    public void testTicketIsExpiredByCount() {
        for (int i = 0; i < NUMBER_OF_USES; i++)
            this.ticket.incrementCountOfUses();

        assertTrue(this.ticket.isExpired());
    }
}