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
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public abstract class AbstractRegistryCleanerTests {
    protected TicketRegistry ticketRegistry;

    private RegistryCleaner registryCleaner;


    @Before
    public void setUp() throws Exception {
        this.ticketRegistry = this.getNewTicketRegistry();
        this.registryCleaner = this.getNewRegistryCleaner(this.ticketRegistry);
    }

    public abstract RegistryCleaner getNewRegistryCleaner(TicketRegistry newTicketRegistry);

    public abstract TicketRegistry getNewTicketRegistry();

    @Test
    public void verifyCleanEmptyTicketRegistry() {
        clean();
        assertTrue(this.ticketRegistry.getTickets().isEmpty());
    }

    @Test
    public void verifyCleanRegistryOfExpiredTicketsAllExpired() {
        populateRegistryWithExpiredTickets();
        clean();
        assertTrue(this.ticketRegistry.getTickets().isEmpty());
    }

    @Test
    public void verifyCleanRegistryOneNonExpired() {
        populateRegistryWithExpiredTickets();
        final TicketGrantingTicket ticket = new TicketGrantingTicketImpl("testNoExpire", TestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        this.ticketRegistry.addTicket(ticket);
        clean();
        assertEquals(this.ticketRegistry.getTickets().size(), 1);
    }

    protected void populateRegistryWithExpiredTickets() {
        for (int i = 0; i < 10; i++) {
            final TicketGrantingTicket ticket = new TicketGrantingTicketImpl("test" + i, TestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy());
            ticket.markTicketExpired();
            this.ticketRegistry.addTicket(ticket);
        }
    }

    private void clean() {
        beforeCleaning();
        afterCleaning(this.registryCleaner.clean());
    }
    protected void beforeCleaning() {}
    protected void afterCleaning(final Collection<Ticket> removedCol) {}
}
