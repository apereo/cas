/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.ImmutableAuthentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.UniqueTicketIdGenerator;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Id: ServiceTicketImplTests.java,v 1.4 2005/02/27 05:49:26
 * sbattaglia Exp $
 */
public class ServiceTicketImplTests extends TestCase {

    private TicketGrantingTicket ticketGrantingTicket = new TicketGrantingTicketImpl(
        "test", new ImmutableAuthentication(new SimplePrincipal("test"), null),
        new NeverExpiresExpirationPolicy());
    
    private UniqueTicketIdGenerator uniqueTicketIdGenerator = new DefaultUniqueTicketIdGenerator();

    public void testNoService() {
        try {
            new ServiceTicketImpl("stest1", this.ticketGrantingTicket, null,
                true, new NeverExpiresExpirationPolicy());
            fail("Exception expected.");
        }
        catch (Exception e) {
            // ok
        }
    }

    public void testNoTicket() {
        try {
            new ServiceTicketImpl("stest1", null, new SimpleService("test"),
                true, new NeverExpiresExpirationPolicy());
            fail("Exception expected.");
        }
        catch (Exception e) {
            // ok
        }
    }

    public void testIsFromNewLoginTrue() {
        ServiceTicket s = new ServiceTicketImpl("stest1",
            this.ticketGrantingTicket, new SimpleService("test"), true,
            new NeverExpiresExpirationPolicy());
        assertTrue(s.isFromNewLogin());
    }

    public void testIsFromNewLoginFalse() {
        ServiceTicket s = new ServiceTicketImpl("stest1",
            this.ticketGrantingTicket, new SimpleService("test"), false,
            new NeverExpiresExpirationPolicy());
        assertFalse(s.isFromNewLogin());
    }

    public void testIsFromNewLoginFalseManualSet() {
        ServiceTicket s = new ServiceTicketImpl("stest1",
            this.ticketGrantingTicket, new SimpleService("test"), false,
            new NeverExpiresExpirationPolicy());
        s.setFromNewLogin(false);
        assertFalse(s.isFromNewLogin());
    }

    public void testIsFromNewLoginFalseManualSetTrue() {
        ServiceTicket s = new ServiceTicketImpl("stest1",
            this.ticketGrantingTicket, new SimpleService("test"), false,
            new NeverExpiresExpirationPolicy());
        s.setFromNewLogin(true);
        assertTrue(s.isFromNewLogin());
    }

    public void testGetService() {
        Service simpleService = new SimpleService("test");
        ServiceTicket s = new ServiceTicketImpl("stest1",
            this.ticketGrantingTicket, simpleService, false,
            new NeverExpiresExpirationPolicy());
        assertEquals(simpleService, s.getService());
    }

    public void testGetTicket() {
        Service simpleService = new SimpleService("test");
        ServiceTicket s = new ServiceTicketImpl("stest1",
            this.ticketGrantingTicket, simpleService, false,
            new NeverExpiresExpirationPolicy());
        assertEquals(this.ticketGrantingTicket, s.getGrantingTicket());
    }

    public void testIsExpiredTrueBecauseOfRoot() {
        TicketGrantingTicket t = new TicketGrantingTicketImpl("test",
            new ImmutableAuthentication(new SimplePrincipal("test"), null),
            new NeverExpiresExpirationPolicy());
        ServiceTicket s = t.grantServiceTicket(this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),new SimpleService("test"), new NeverExpiresExpirationPolicy());
        t.expire();

        assertTrue(s.isExpired());
    }

    public void testIsExpiredTrueBecauseOfCount() {
        TicketGrantingTicket t = new TicketGrantingTicketImpl("test",
            new ImmutableAuthentication(new SimplePrincipal("test"), null),
            new NeverExpiresExpirationPolicy());
        ServiceTicket s = t.grantServiceTicket(this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX), new SimpleService("test"), new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000));
        s.incrementCountOfUses();
        assertTrue(s.isExpired());
    }

    public void testIsExpiredFalse() {
        TicketGrantingTicket t = new TicketGrantingTicketImpl("test",
            new ImmutableAuthentication(new SimplePrincipal("test"), null),
            new NeverExpiresExpirationPolicy());
        ServiceTicket s = t.grantServiceTicket(this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX), new SimpleService("test"),new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000));
        assertFalse(s.isExpired());
    }

    public void testTicketGrantingTicket() {
        Authentication a = new ImmutableAuthentication(new SimplePrincipal(
            "test"), null);
        TicketGrantingTicket t = new TicketGrantingTicketImpl("test",
            new ImmutableAuthentication(new SimplePrincipal("test"), null),
            new NeverExpiresExpirationPolicy());
        ServiceTicket s = t.grantServiceTicket(this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),new SimpleService("test"),new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000));
        TicketGrantingTicket t1 = s.grantTicketGrantingTicket(this.uniqueTicketIdGenerator.getNewTicketId(TicketGrantingTicket.PREFIX),a,new NeverExpiresExpirationPolicy());

        assertEquals(a, t1.getAuthentication());
    }
}