/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.ImmutableAuthentication;
import org.jasig.cas.authentication.Service;
import org.jasig.cas.authentication.SimpleService;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class ServiceTicketImplTests extends TestCase {
    private TicketGrantingTicket t = new TicketGrantingTicketImpl("test", new ImmutableAuthentication(new SimplePrincipal("test"), null), new NeverExpiresExpirationPolicy(), new DefaultUniqueTicketIdGenerator(), new NeverExpiresExpirationPolicy());
    
    public void testNoService() {
        try {
            new ServiceTicketImpl("stest1", this.t, null, true, new NeverExpiresExpirationPolicy(), new DefaultUniqueTicketIdGenerator(), new NeverExpiresExpirationPolicy());
           fail("Exception expected.");
        } catch (Exception e) {
            // ok
        }
    }
    
    public void testNoTicket() {
        try {
            new ServiceTicketImpl("stest1", null, new SimpleService("test"), true, new NeverExpiresExpirationPolicy(), new DefaultUniqueTicketIdGenerator(), new NeverExpiresExpirationPolicy());
           fail("Exception expected.");
        } catch (Exception e) {
            // ok
        }
    }
    
    public void testIsFromNewLoginTrue() {
        ServiceTicket s = new ServiceTicketImpl("stest1", this.t, new SimpleService("test"), true, new NeverExpiresExpirationPolicy(), new DefaultUniqueTicketIdGenerator(), new NeverExpiresExpirationPolicy());
        assertTrue(s.isFromNewLogin());
    }
    
    public void testIsFromNewLoginFalse() {
        ServiceTicket s = new ServiceTicketImpl("stest1", this.t, new SimpleService("test"), false, new NeverExpiresExpirationPolicy(), new DefaultUniqueTicketIdGenerator(), new NeverExpiresExpirationPolicy());
        assertFalse(s.isFromNewLogin());
    }
    
    public void testIsFromNewLoginFalseManualSet() {
        ServiceTicket s = new ServiceTicketImpl("stest1", this.t, new SimpleService("test"), false, new NeverExpiresExpirationPolicy(), new DefaultUniqueTicketIdGenerator(), new NeverExpiresExpirationPolicy());
        s.setFromNewLogin(false);
        assertFalse(s.isFromNewLogin());
    }
    
    public void testIsFromNewLoginFalseManualSetTrue() {
        ServiceTicket s = new ServiceTicketImpl("stest1", this.t, new SimpleService("test"), false, new NeverExpiresExpirationPolicy(), new DefaultUniqueTicketIdGenerator(), new NeverExpiresExpirationPolicy());
        s.setFromNewLogin(true);
        assertTrue(s.isFromNewLogin());
    }
    
    public void testGetService() {
        Service simpleService = new SimpleService("test");
        ServiceTicket s = new ServiceTicketImpl("stest1", this.t, simpleService, false, new NeverExpiresExpirationPolicy(), new DefaultUniqueTicketIdGenerator(), new NeverExpiresExpirationPolicy());
        assertEquals(simpleService, s.getService());
    }
    
    public void testGetTicket() {
        Service simpleService = new SimpleService("test");
        ServiceTicket s = new ServiceTicketImpl("stest1", this.t, simpleService, false, new NeverExpiresExpirationPolicy(), new DefaultUniqueTicketIdGenerator(), new NeverExpiresExpirationPolicy());
        assertEquals(this.t, s.getGrantingTicket());
    }
    
    public void testIsExpiredTrueBecauseOfRoot() {
        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", new ImmutableAuthentication(new SimplePrincipal("test"), null), new NeverExpiresExpirationPolicy(), new DefaultUniqueTicketIdGenerator(), new NeverExpiresExpirationPolicy());
        ServiceTicket s = t.grantServiceTicket(new SimpleService("test"));
        t.expire();
        
        assertTrue(s.isExpired());
    }
    
    public void testIsExpiredTrueBecauseOfCount() {
        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", new ImmutableAuthentication(new SimplePrincipal("test"), null), new NeverExpiresExpirationPolicy(), new DefaultUniqueTicketIdGenerator(), new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000));
        ServiceTicket s = t.grantServiceTicket(new SimpleService("test"));
        s.incrementCountOfUses();
        assertTrue(s.isExpired());
    }
    
    public void testIsExpiredFalse() {
        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", new ImmutableAuthentication(new SimplePrincipal("test"), null), new NeverExpiresExpirationPolicy(), new DefaultUniqueTicketIdGenerator(), new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000));
        ServiceTicket s = t.grantServiceTicket(new SimpleService("test"));
        assertFalse(s.isExpired());
    }
    
    public void testTicketGrantingTicket() {
        Authentication a = new ImmutableAuthentication(new SimplePrincipal("test"), null);
        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", new ImmutableAuthentication(new SimplePrincipal("test"), null), new NeverExpiresExpirationPolicy(), new DefaultUniqueTicketIdGenerator(), new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000));
        ServiceTicket s = t.grantServiceTicket(new SimpleService("test"));
        TicketGrantingTicket t1 = s.grantTicketGrantingTicket(a);
        
        assertEquals(a, t1.getAuthentication());
    }
}