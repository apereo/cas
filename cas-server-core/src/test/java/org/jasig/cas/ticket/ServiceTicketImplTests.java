/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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

import static org.junit.Assert.*;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.junit.Test;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class ServiceTicketImplTests {

    private final TicketGrantingTicketImpl ticketGrantingTicket = new TicketGrantingTicketImpl("test",
            TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());

    private final UniqueTicketIdGenerator uniqueTicketIdGenerator = new DefaultUniqueTicketIdGenerator();

    @Test(expected = Exception.class)
    public void verifyNoService() {
        new ServiceTicketImpl("stest1", this.ticketGrantingTicket, null, true, new NeverExpiresExpirationPolicy());
    }

    @Test(expected = Exception.class)
    public void verifyNoTicket() {
        new ServiceTicketImpl("stest1", null, TestUtils.getService(), true, new NeverExpiresExpirationPolicy());
    }

    @Test
    public void verifyIsFromNewLoginTrue() {
        final ServiceTicket s = new ServiceTicketImpl("stest1", this.ticketGrantingTicket, TestUtils.getService(), true,
                new NeverExpiresExpirationPolicy());
        assertTrue(s.isFromNewLogin());
    }

    @Test
    public void verifyIsFromNewLoginFalse() {
        final ServiceTicket s = new ServiceTicketImpl("stest1", this.ticketGrantingTicket, TestUtils.getService(), false,
                new NeverExpiresExpirationPolicy());
        assertFalse(s.isFromNewLogin());
    }

    @Test
    public void verifyGetService() {
        final Service simpleService = TestUtils.getService();
        final ServiceTicket s = new ServiceTicketImpl("stest1", this.ticketGrantingTicket, simpleService, false,
                new NeverExpiresExpirationPolicy());
        assertEquals(simpleService, s.getService());
    }

    @Test
    public void verifyGetTicket() {
        final Service simpleService = TestUtils.getService();
        final ServiceTicket s = new ServiceTicketImpl("stest1", this.ticketGrantingTicket, simpleService, false,
                new NeverExpiresExpirationPolicy());
        assertEquals(this.ticketGrantingTicket, s.getGrantingTicket());
    }

    @Test
    public void verifyIsExpiredTrueBecauseOfRoot() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", TestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        final ServiceTicket s = t.grantServiceTicket(this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),
                TestUtils.getService(), new NeverExpiresExpirationPolicy(), false);
        t.markTicketExpired();

        assertTrue(s.isExpired());
    }

    @Test
    public void verifyIsExpiredFalse() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", TestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        final ServiceTicket s = t.grantServiceTicket(this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),
                TestUtils.getService(), new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000), false);
        assertFalse(s.isExpired());
    }

    @Test
    public void verifyTicketGrantingTicket() {
        final Authentication a = TestUtils.getAuthentication();
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", TestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        final ServiceTicket s = t.grantServiceTicket(this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),
                TestUtils.getService(), new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000), false);
        final TicketGrantingTicket t1 = s.grantTicketGrantingTicket(
                this.uniqueTicketIdGenerator.getNewTicketId(TicketGrantingTicket.PREFIX), a,
                new NeverExpiresExpirationPolicy());

        assertEquals(a, t1.getAuthentication());
    }

    @Test
    public void verifyTicketGrantingTicketGrantedTwice() {
        final Authentication a = TestUtils.getAuthentication();
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", TestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        final ServiceTicket s = t.grantServiceTicket(this.uniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX),
                TestUtils.getService(), new MultiTimeUseOrTimeoutExpirationPolicy(1, 5000), false);
        s.grantTicketGrantingTicket(this.uniqueTicketIdGenerator.getNewTicketId(TicketGrantingTicket.PREFIX), a,
                new NeverExpiresExpirationPolicy());

        try {
            s.grantTicketGrantingTicket(this.uniqueTicketIdGenerator.getNewTicketId(TicketGrantingTicket.PREFIX), a,
                    new NeverExpiresExpirationPolicy());
            fail("Exception expected.");
        } catch (final Exception e) {
            return;
        }
    }
}
