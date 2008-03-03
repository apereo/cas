/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.ticket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Response;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.WebApplicationService;
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

    public void testEquals() {
        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null,
            TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        assertFalse(t.equals(null));
        assertFalse(t.equals(new Object()));
        assertTrue(t.equals(t));
    }
    
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
        Authentication authentication = TestUtils.getAuthentication();

        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null,
            authentication, new NeverExpiresExpirationPolicy());

        assertEquals(t.getAuthentication(), authentication);
        assertEquals(t.getId(), t.toString());
    }

    public void testIsRootTrue() {
        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null,
            TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        assertTrue(t.isRoot());
    }

    public void testIsRootFalse() {
        TicketGrantingTicketImpl t1 = new TicketGrantingTicketImpl("test", null,
            TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", t1,
            TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        assertFalse(t.isRoot());
    }

    public void testGetChainedPrincipalsWithOne() {
        Authentication authentication = TestUtils.getAuthentication();
        List<Authentication> principals = new ArrayList<Authentication>();
        principals.add(authentication);

        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null,
            authentication, new NeverExpiresExpirationPolicy());

        assertEquals(principals, t.getChainedAuthentications());
    }
    
    public void testCheckCreationTime() {
        Authentication authentication = TestUtils.getAuthentication();
        List<Authentication> principals = new ArrayList<Authentication>();
        principals.add(authentication);
        
        final long startTime = System.currentTimeMillis();
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null,
            authentication, new NeverExpiresExpirationPolicy());
        final long finishTime = System.currentTimeMillis();
        
        assertTrue(startTime <= t.getCreationTime() && finishTime >= t.getCreationTime());
    }

    public void testGetChainedPrincipalsWithTwo() {
        Authentication authentication = TestUtils.getAuthentication();
        Authentication authentication1 = TestUtils.getAuthentication("test1");
        List<Authentication> principals = new ArrayList<Authentication>();
        principals.add(authentication);
        principals.add(authentication1);

        TicketGrantingTicketImpl t1 = new TicketGrantingTicketImpl("test", null,
            authentication1, new NeverExpiresExpirationPolicy());
        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", t1,
            authentication, new NeverExpiresExpirationPolicy());

        assertEquals(principals, t.getChainedAuthentications());
    }

    public void testServiceTicketAsFromInitialCredentials() {
        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null,
            TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        ServiceTicket s = t.grantServiceTicket(this.uniqueTicketIdGenerator
            .getNewTicketId(ServiceTicket.PREFIX), TestUtils.getService(),
            new NeverExpiresExpirationPolicy(), false);

        assertTrue(s.isFromNewLogin());
    }

    public void testServiceTicketAsFromNotInitialCredentials() {
        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null,
            TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        ServiceTicket s = t.grantServiceTicket(this.uniqueTicketIdGenerator
            .getNewTicketId(ServiceTicket.PREFIX), TestUtils.getService(),
            new NeverExpiresExpirationPolicy(), false);
        s = t.grantServiceTicket(this.uniqueTicketIdGenerator
            .getNewTicketId(ServiceTicket.PREFIX), TestUtils.getService(),
            new NeverExpiresExpirationPolicy(), false);

        assertFalse(s.isFromNewLogin());
    }
    
    public void testWebApplicationSignOut() {
        final TestService testService = new TestService();
        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null,
            TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        t.grantServiceTicket(this.uniqueTicketIdGenerator
            .getNewTicketId(ServiceTicket.PREFIX), testService,
            new NeverExpiresExpirationPolicy(), false);
        
        t.expire();
        
        assertTrue(testService.isLoggedOut());
    }
    
    protected static class TestService implements WebApplicationService {
        
        /**
         * Comment for <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = -8318147503545267651L;
        private boolean loggedOut = false;

        public String getArtifactId() {
            return null;
        }

        public Response getResponse(String ticketId) {
            return null;
        }

        public boolean logOutOfService(final String sessionIdentifier) {
            this.loggedOut = true;
            return false;
        }
        
        public boolean isLoggedOut() {
            return this.loggedOut;
        }

        public void setPrincipal(Principal principal) {
            // nothing to do
        }

        public Map<String, Object> getAttributes() {
            return null;
        }

        public String getId() {
            return "test";
        }
        
        public boolean matches(final Service service) {
            return true;
        }
    }
}
