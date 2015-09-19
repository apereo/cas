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
package org.jasig.cas.ticket.registry;

import org.jasig.cas.TestUtils;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 *
 * @author Scott Battaglia
 * @since 3.1
 *
 */
public final class DistributedTicketRegistryTests {

    private TestDistributedTicketRegistry ticketRegistry;

    private boolean wasTicketUpdated;

    public void setWasTicketUpdated(final boolean wasTicketUpdated) {
        this.wasTicketUpdated = wasTicketUpdated;
    }

    @Before
    public void setUp() throws Exception {
        this.ticketRegistry = new TestDistributedTicketRegistry(this);
        this.wasTicketUpdated = false;
    }

    @Test
    public void verifyProxiedInstancesEqual() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", TestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        this.ticketRegistry.addTicket(t);

        final TicketGrantingTicket returned = (TicketGrantingTicket) this.ticketRegistry.getTicket("test");
        assertEquals(t, returned);
        assertEquals(returned, t);

        assertEquals(t.getCreationTime(), returned.getCreationTime());
        assertEquals(t.getAuthentication(), returned.getAuthentication());
        assertEquals(t.getCountOfUses(), returned.getCountOfUses());
        assertEquals(t.getGrantingTicket(), returned.getGrantingTicket());
        assertEquals(t.getId(), returned.getId());
        assertEquals(t.getChainedAuthentications(), returned.getChainedAuthentications());
        assertEquals(t.isExpired(), returned.isExpired());
        assertEquals(t.isRoot(), returned.isRoot());

        final ServiceTicket s = t.grantServiceTicket("stest", TestUtils.getService(),
                new NeverExpiresExpirationPolicy(), false);
        this.ticketRegistry.addTicket(s);

        final ServiceTicket sreturned = (ServiceTicket) this.ticketRegistry.getTicket("stest");
        assertEquals(s, sreturned);
        assertEquals(sreturned, s);

        assertEquals(s.getCreationTime(), sreturned.getCreationTime());
        assertEquals(s.getCountOfUses(), sreturned.getCountOfUses());
        assertEquals(s.getGrantingTicket(), sreturned.getGrantingTicket());
        assertEquals(s.getId(), sreturned.getId());
        assertEquals(s.isExpired(), sreturned.isExpired());
        assertEquals(s.getService(), sreturned.getService());
        assertEquals(s.isFromNewLogin(), sreturned.isFromNewLogin());
    }

    @Test
    public void verifyUpdateOfRegistry() {
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("test", TestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        this.ticketRegistry.addTicket(t);
        final TicketGrantingTicket returned = (TicketGrantingTicket) this.ticketRegistry.getTicket("test");

        final ServiceTicket s = returned.grantServiceTicket("test2", TestUtils.getService(),
                new NeverExpiresExpirationPolicy(), true);

        this.ticketRegistry.addTicket(s);
        final ServiceTicket s2 = (ServiceTicket) this.ticketRegistry.getTicket("test2");
        assertNotNull(s2.grantTicketGrantingTicket("ff", TestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy()));

        assertTrue(s2.isValidFor(TestUtils.getService()));
        assertTrue(this.wasTicketUpdated);

        returned.markTicketExpired();
        assertTrue(t.isExpired());
    }

    @Test
    public void verifyTicketDoesntExist() {
        assertNull(this.ticketRegistry.getTicket("fdfas"));
    }

    private static class TestDistributedTicketRegistry extends AbstractDistributedTicketRegistry {
        private final DistributedTicketRegistryTests parent;
        private final Map<String, Ticket> tickets = new HashMap<>();

        TestDistributedTicketRegistry(final DistributedTicketRegistryTests parent) {
            this.parent = parent;
        }

        protected void updateTicket(final Ticket ticket) {
            this.parent.setWasTicketUpdated(true);
        }

        public void addTicket(final Ticket ticket) {
            this.tickets.put(ticket.getId(), ticket);
        }

        public boolean deleteTicket(final String ticketId) {
            return this.tickets.remove(ticketId) != null;
        }

        public Ticket getTicket(final String ticketId) {
            return getProxiedTicketInstance(this.tickets.get(ticketId));
        }

        public Collection<Ticket> getTickets() {
            return this.tickets.values();
        }

        @Override
        protected boolean needsCallback() {
            return true;
        }
    }
}
