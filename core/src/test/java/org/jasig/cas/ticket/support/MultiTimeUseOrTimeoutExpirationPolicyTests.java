/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.support;

import org.jasig.cas.authentication.ImmutableAuthentication;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;

import junit.framework.TestCase;


public class MultiTimeUseOrTimeoutExpirationPolicyTests extends TestCase {
    private static final long TIMEOUT = 5000;
    private static final int NUMBER_OF_USES = 5;
    private ExpirationPolicy expirationPolicy;

    protected void setUp() throws Exception {
        this.expirationPolicy = new MultiTimeUseOrTimeoutExpirationPolicy(NUMBER_OF_USES, TIMEOUT);
        super.setUp();
    }
    
    public void testTicketIsNull() {
        assertTrue(this.expirationPolicy.isExpired(null));
    }
    
    public void testTicketIsNotExpired() {
        Ticket ticket = new TicketGrantingTicketImpl("test", new ImmutableAuthentication(new SimplePrincipal("test"), null), this.expirationPolicy, new DefaultUniqueTicketIdGenerator(), this.expirationPolicy);
        assertFalse(ticket.isExpired());
    }
    
    public void testTicketIsExpiredByTime() {
        try {
            Ticket ticket = new TicketGrantingTicketImpl("test", new ImmutableAuthentication(new SimplePrincipal("test"), null), this.expirationPolicy, new DefaultUniqueTicketIdGenerator(), this.expirationPolicy);
            Thread.sleep(TIMEOUT+15);
            assertTrue(ticket.isExpired());
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }
    
    public void testTicketIsExpiredByCount() {
        Ticket ticket = new TicketGrantingTicketImpl("test", new ImmutableAuthentication(new SimplePrincipal("test"), null), this.expirationPolicy, new DefaultUniqueTicketIdGenerator(), this.expirationPolicy);
        
        for (int i =0; i < NUMBER_OF_USES; i++)
            ticket.incrementCountOfUses();
        
        assertTrue(ticket.isExpired());
    }
}
