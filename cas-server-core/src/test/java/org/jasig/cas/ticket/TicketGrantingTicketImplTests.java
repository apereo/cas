/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.ticket;

import java.util.ArrayList;
import java.util.List;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.mock.MockService;
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
        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null, null,
            TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        assertFalse(t.equals(null));
        assertFalse(t.equals(new Object()));
        assertTrue(t.equals(t));
    }
    
    public void testNullAuthentication() {
        try {
            new TicketGrantingTicketImpl("test", null, null, null,
                new NeverExpiresExpirationPolicy());
            fail("Exception expected.");
        } catch (Exception e) {
            // this is okay
        }
    }

    public void testGetAuthentication() {
        Authentication authentication = TestUtils.getAuthentication();

        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null, null,
            authentication, new NeverExpiresExpirationPolicy());

        assertEquals(t.getAuthentication(), authentication);
        assertEquals(t.getId(), t.toString());
    }

    public void testIsRootTrue() {
        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null, null,
            TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        assertTrue(t.isRoot());
    }

    public void testIsRootFalse() {
        TicketGrantingTicketImpl t1 = new TicketGrantingTicketImpl("test", null, null,
            TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", "grantor", t1,
            TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        assertFalse(t.isRoot());
    }

    public void testGetChainedPrincipalsWithOne() {
        Authentication authentication = TestUtils.getAuthentication();
        List<Authentication> principals = new ArrayList<Authentication>();
        principals.add(authentication);

        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null, null,
            authentication, new NeverExpiresExpirationPolicy());

        assertEquals(principals, t.getChainedAuthentications());
    }
    
    public void testCheckCreationTime() {
        Authentication authentication = TestUtils.getAuthentication();
        List<Authentication> principals = new ArrayList<Authentication>();
        principals.add(authentication);
        
        final long startTime = System.currentTimeMillis();
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null, null,
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

        TicketGrantingTicketImpl t1 = new TicketGrantingTicketImpl("test", null, null,
            authentication1, new NeverExpiresExpirationPolicy());
        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", "grantor", t1,
            authentication, new NeverExpiresExpirationPolicy());

        assertEquals(principals, t.getChainedAuthentications());
    }

    public void testServiceTicketAsFromInitialCredentials() {
        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null, null,
            TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        ServiceTicket s = t.grantServiceTicket(this.uniqueTicketIdGenerator
            .getNewTicketId(ServiceTicket.PREFIX), TestUtils.getService(),
            new NeverExpiresExpirationPolicy(), false);

        assertTrue(s.isFromNewLogin());
    }

    public void testServiceTicketAsFromNotInitialCredentials() {
        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null, null,
            TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        t.grantServiceTicket(
                this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),
                TestUtils.getService(),
                new NeverExpiresExpirationPolicy(),
                false);
        ServiceTicket s = t.grantServiceTicket(
                this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),
                TestUtils.getService(),
                new NeverExpiresExpirationPolicy(),
                false);

        assertFalse(s.isFromNewLogin());
    }
    
    public void testWebApplicationSignOut() {
        final MockService testService = new MockService("test");
        TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null, null,
            TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        t.grantServiceTicket(this.uniqueTicketIdGenerator
            .getNewTicketId(ServiceTicket.PREFIX), testService,
            new NeverExpiresExpirationPolicy(), false);
        
        t.expire();
        
        assertTrue(testService.isLoggedOut());
    }
}
