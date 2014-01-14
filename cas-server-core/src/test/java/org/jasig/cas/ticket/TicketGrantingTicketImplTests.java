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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.mock.MockService;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.junit.Test;

/**
 * @author Scott Battaglia

 * @since 3.0
 */
public class TicketGrantingTicketImplTests {

    private final UniqueTicketIdGenerator uniqueTicketIdGenerator = new DefaultUniqueTicketIdGenerator();

    @Test
    public void testEquals() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null,
            TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        assertFalse(t.equals(null));
        assertFalse(t.equals(new Object()));
        assertTrue(t.equals(t));
    }

    @Test(expected=Exception.class)
    public void testNullAuthentication() {
        new TicketGrantingTicketImpl("test", null, null,
                new NeverExpiresExpirationPolicy());
    }

    @Test
    public void testGetAuthentication() {
        final Authentication authentication = TestUtils.getAuthentication();

        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null,
            authentication, new NeverExpiresExpirationPolicy());

        assertEquals(t.getAuthentication(), authentication);
        assertEquals(t.getId(), t.toString());
    }

    @Test
    public void testIsRootTrue() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null,
            TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        assertTrue(t.isRoot());
    }

    @Test
    public void testIsRootFalse() {
        final TicketGrantingTicketImpl t1 = new TicketGrantingTicketImpl("test", null,
            TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", t1,
            TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

        assertFalse(t.isRoot());
    }

    @Test
    public void testGetChainedPrincipalsWithOne() {
        final Authentication authentication = TestUtils.getAuthentication();
        final List<Authentication> principals = new ArrayList<Authentication>();
        principals.add(authentication);

        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null,
            authentication, new NeverExpiresExpirationPolicy());

        assertEquals(principals, t.getChainedAuthentications());
    }

    @Test
    public void testCheckCreationTime() {
        final Authentication authentication = TestUtils.getAuthentication();
        final List<Authentication> principals = new ArrayList<Authentication>();
        principals.add(authentication);

        final long startTime = System.currentTimeMillis();
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null,
            authentication, new NeverExpiresExpirationPolicy());
        final long finishTime = System.currentTimeMillis();
        assertTrue(startTime <= t.getCreationTime() && finishTime >= t.getCreationTime());
    }

    @Test
    public void testGetChainedPrincipalsWithTwo() {
        final Authentication authentication = TestUtils.getAuthentication();
        final Authentication authentication1 = TestUtils.getAuthentication("test1");
        final List<Authentication> principals = new ArrayList<Authentication>();
        principals.add(authentication);
        principals.add(authentication1);

        final TicketGrantingTicketImpl t1 = new TicketGrantingTicketImpl("test", null,
            authentication1, new NeverExpiresExpirationPolicy());
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", t1,
            authentication, new NeverExpiresExpirationPolicy());

        assertEquals(principals, t.getChainedAuthentications());
    }

    @Test
    public void testServiceTicketAsFromInitialCredentials() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null,
            TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        final ServiceTicket s = t.grantServiceTicket(this.uniqueTicketIdGenerator
            .getNewTicketId(ServiceTicket.PREFIX), TestUtils.getService(),
            new NeverExpiresExpirationPolicy(), false);

        assertTrue(s.isFromNewLogin());
    }

    @Test
    public void testServiceTicketAsFromNotInitialCredentials() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null,
            TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        ServiceTicket s = t.grantServiceTicket(this.uniqueTicketIdGenerator
            .getNewTicketId(ServiceTicket.PREFIX), TestUtils.getService(),
            new NeverExpiresExpirationPolicy(), false);
        s = t.grantServiceTicket(this.uniqueTicketIdGenerator
            .getNewTicketId(ServiceTicket.PREFIX), TestUtils.getService(),
            new NeverExpiresExpirationPolicy(), false);

        assertFalse(s.isFromNewLogin());
    }

    @Test
    public void testWebApplicationServices() {
        final MockService testService = new MockService("test");
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null,
            TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        t.grantServiceTicket(this.uniqueTicketIdGenerator
            .getNewTicketId(ServiceTicket.PREFIX), testService,
            new NeverExpiresExpirationPolicy(), false);
        Map<String, Service> services = t.getServices();
        assertEquals(1, services.size());
        final String ticketId = services.keySet().iterator().next();
        assertEquals(testService, services.get(ticketId));
        t.removeAllServices();
        services = t.getServices();
        assertEquals(0, services.size());
    }

    @Test
    public void testWebApplicationExpire() {
        final MockService testService = new MockService("test");
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", null,
            TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
        t.grantServiceTicket(this.uniqueTicketIdGenerator
            .getNewTicketId(ServiceTicket.PREFIX), testService,
            new NeverExpiresExpirationPolicy(), false);
        assertFalse(t.isExpired());
        t.markTicketExpired();
        assertTrue(t.isExpired());
    }
}
