/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.mock.MockAuthentication;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.UniqueTicketIdGenerator;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class TicketGrantingTicketImplTests extends TestCase {

    private UniqueTicketIdGenerator uniqueTicketIdGenerator = new DefaultUniqueTicketIdGenerator();

    public void testNullAuthentication() {
        try {
            new TicketGrantingTicketImpl("test", null, null,
                new NeverExpiresExpirationPolicy());
            fail("Exception expected.");
        } catch (Exception e) {
            // this is okay
        }
    }

    public void testGetAuthentication() {
        Authentication authentication = new MockAuthentication();

        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null,
            authentication, new NeverExpiresExpirationPolicy());

        assertEquals(t.getAuthentication(), authentication);
    }

    public void testIsRootTrue() {
        Authentication authentication = new MockAuthentication();

        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null,
            authentication, new NeverExpiresExpirationPolicy());

        assertTrue(t.isRoot());
    }

    public void testIsRootFalse() {
        Authentication authentication = new MockAuthentication();
        TicketGrantingTicket t1 = new TicketGrantingTicketImpl("test", null,
            authentication, new NeverExpiresExpirationPolicy());
        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", t1,
            authentication, new NeverExpiresExpirationPolicy());

        assertFalse(t.isRoot());
    }

    public void testGetChainedPrincipalsWithOne() {
        Principal principal = new SimplePrincipal("Test");
        Authentication authentication = new MockAuthentication(principal);
        List principals = new ArrayList();
        principals.add(authentication);

        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null,
            authentication, new NeverExpiresExpirationPolicy());

        assertEquals(principals, t.getChainedAuthentications());
    }

    public void testGetChainedPrincipalsWithTwo() {
        Principal principal = new SimplePrincipal("Test");
        Principal principal1 = new SimplePrincipal("Test1");
        Authentication authentication = new MockAuthentication(principal);
        Authentication authentication1 = new MockAuthentication(principal1);
        List principals = new ArrayList();
        principals.add(authentication);
        principals.add(authentication1);

        TicketGrantingTicket t1 = new TicketGrantingTicketImpl("test", null,
            authentication1, new NeverExpiresExpirationPolicy());
        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", t1,
            authentication, new NeverExpiresExpirationPolicy());

        assertEquals(principals, t.getChainedAuthentications());
    }

    public void testServiceTicketAsFromInitialCredentials() {
        Authentication authentication = new MockAuthentication();

        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null,
            authentication, new NeverExpiresExpirationPolicy());
        ServiceTicket s = t.grantServiceTicket(this.uniqueTicketIdGenerator
            .getNewTicketId(ServiceTicket.PREFIX), new SimpleService("test"),
            new NeverExpiresExpirationPolicy());

        assertTrue(s.isFromNewLogin());
        assertEquals(t.getCountOfUses(), 1);
    }

    public void testServiceTicketAsFromNotInitialCredentials() {
        Authentication authentication = new MockAuthentication();

        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null,
            authentication, new NeverExpiresExpirationPolicy());
        ServiceTicket s = t.grantServiceTicket(this.uniqueTicketIdGenerator
            .getNewTicketId(ServiceTicket.PREFIX), new SimpleService("test"),
            new NeverExpiresExpirationPolicy());
        s = t.grantServiceTicket(this.uniqueTicketIdGenerator
            .getNewTicketId(ServiceTicket.PREFIX), new SimpleService("test"),
            new NeverExpiresExpirationPolicy());

        assertFalse(s.isFromNewLogin());
        assertEquals(t.getCountOfUses(), 2);
    }

    public void testHashCode() {
        Authentication authentication = new MockAuthentication();

        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null,
            authentication, new NeverExpiresExpirationPolicy());

        assertEquals(HashCodeBuilder.reflectionHashCode(t), t.hashCode());
    }

    public void testToString() {
        Authentication authentication = new MockAuthentication();

        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null,
            authentication, new NeverExpiresExpirationPolicy());

        assertEquals(ToStringBuilder.reflectionToString(t), t.toString());
    }

    public void testIncrementTimeUpdated() {
        Authentication authentication = new MockAuthentication();

        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null,
            authentication, new NeverExpiresExpirationPolicy());

        t.updateLastTimeUsed();
        assertEquals(t.getLastTimeUsed(), System.currentTimeMillis());
    }

    public void testNoIdOrPolicy() {
        Authentication authentication = new MockAuthentication();
        try {
            new TicketGrantingTicketImpl(null, null, authentication, null);

            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            return;
        }
    }
}
