/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

import java.util.ArrayList;
import java.util.List;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.ImmutableAuthentication;
import org.jasig.cas.authentication.SimpleService;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;

import junit.framework.TestCase;


public class TicketGrantingTicketImplTests extends TestCase {

    /* public TicketGrantingTicketImpl(final String id, final TicketGrantingTicket ticketGrantingTicket, final Authentication authentication,
 36         final ExpirationPolicy policy, final UniqueTicketIdGenerator uniqueTicketIdGenerator, final ExpirationPolicy serviceExpirationPolicy) */
    public void testNullAuthentication() {
        try {
            new TicketGrantingTicketImpl("test", null, null, new NeverExpiresExpirationPolicy(), new DefaultUniqueTicketIdGenerator(), new NeverExpiresExpirationPolicy());
            fail("Exception expected.");
        } catch (Exception e) {
            // this is okay
        }
    }
    
    public void testNullServiceExpirationPolicy() {
        try {
            new TicketGrantingTicketImpl("test", null, new ImmutableAuthentication(new SimplePrincipal("Test"), null), new NeverExpiresExpirationPolicy(), new DefaultUniqueTicketIdGenerator(), null);
            fail("Exception expected.");
        } catch (Exception e) {
            // this is okay
        }
    }
    
    public void testNullUniqueTicketIdGenerator() {
        try {
            new TicketGrantingTicketImpl("test", null, new ImmutableAuthentication(new SimplePrincipal("Test"), null), new NeverExpiresExpirationPolicy(), null, new NeverExpiresExpirationPolicy());
            fail("Exception expected.");
        } catch (Exception e) {
            // this is okay
        }
    }
    
    public void testGetAuthentication() {
        Authentication authentication = new ImmutableAuthentication(new SimplePrincipal("Test"), null);

        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null, authentication, new NeverExpiresExpirationPolicy(), new DefaultUniqueTicketIdGenerator(), new NeverExpiresExpirationPolicy());
        
        assertEquals(t.getAuthentication(), authentication);
    }
    
    public void testIsRootTrue() {
        Authentication authentication = new ImmutableAuthentication(new SimplePrincipal("Test"), null);

        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null, authentication, new NeverExpiresExpirationPolicy(), new DefaultUniqueTicketIdGenerator(), new NeverExpiresExpirationPolicy());
        
        assertTrue(t.isRoot());
    }
    
    public void testIsRootFalse() {
        Authentication authentication = new ImmutableAuthentication(new SimplePrincipal("Test"), null);
        TicketGrantingTicket t1 = new TicketGrantingTicketImpl("test", null, authentication, new NeverExpiresExpirationPolicy(), new DefaultUniqueTicketIdGenerator(), new NeverExpiresExpirationPolicy());
        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", t1, authentication, new NeverExpiresExpirationPolicy(), new DefaultUniqueTicketIdGenerator(), new NeverExpiresExpirationPolicy());
        
        assertFalse(t.isRoot());
    }
    
    public void testGetChainedPrincipalsWithOne() {
        Principal principal = new SimplePrincipal("Test");
        Authentication authentication = new ImmutableAuthentication(principal, null);
        List principals = new ArrayList();
        principals.add(principal);

        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null, authentication, new NeverExpiresExpirationPolicy(), new DefaultUniqueTicketIdGenerator(), new NeverExpiresExpirationPolicy());
        
        assertEquals(principals, t.getChainedPrincipals());
    }
    
    public void testGetChainedPrincipalsWithTwo() {
        Principal principal = new SimplePrincipal("Test");
        Principal principal1 = new SimplePrincipal("Test1");
        Authentication authentication = new ImmutableAuthentication(principal, null);
        Authentication authentication1 = new ImmutableAuthentication(principal1, null);
        List principals = new ArrayList();
        principals.add(principal1);
        principals.add(principal);

        TicketGrantingTicket t1 = new TicketGrantingTicketImpl("test", null, authentication1, new NeverExpiresExpirationPolicy(), new DefaultUniqueTicketIdGenerator(), new NeverExpiresExpirationPolicy());
        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", t1, authentication, new NeverExpiresExpirationPolicy(), new DefaultUniqueTicketIdGenerator(), new NeverExpiresExpirationPolicy());

        
        assertEquals(principals, t.getChainedPrincipals());
    }
    
    public void testServiceTicketAsFromInitialCredentials() {
        Authentication authentication = new ImmutableAuthentication(new SimplePrincipal("Test"), null);

        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null, authentication, new NeverExpiresExpirationPolicy(), new DefaultUniqueTicketIdGenerator(), new NeverExpiresExpirationPolicy());
        ServiceTicket s = t.grantServiceTicket(new SimpleService("test"));
        
        assertTrue(s.isFromNewLogin());
        assertEquals(t.getCountOfUses(), 1);
    }
    
    public void testServiceTicketAsFromNotInitialCredentials() {
        Authentication authentication = new ImmutableAuthentication(new SimplePrincipal("Test"), null);

        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null, authentication, new NeverExpiresExpirationPolicy(), new DefaultUniqueTicketIdGenerator(), new NeverExpiresExpirationPolicy());
        ServiceTicket s = t.grantServiceTicket(new SimpleService("test"));
        s = t.grantServiceTicket(new SimpleService("test"));
        
        assertFalse(s.isFromNewLogin());
        assertEquals(t.getCountOfUses(), 2);
    }
    
}
