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
package org.jasig.cas.ticket.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * Unit test for {@link EhCacheTicketRegistry}.
 *
 * @author Scott Battaglia
 * @since 3.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:ticketRegistry.xml")
public final class EhCacheTicketRegistryTests implements ApplicationContextAware {

    private static final int TICKETS_IN_REGISTRY = 10;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ApplicationContext applicationContext = null;
    private TicketRegistry ticketRegistry = null;

    @Before
    public void setUp() throws Exception {
        this.ticketRegistry = this.applicationContext.getBean("ticketRegistry", TicketRegistry.class);
        initTicketRegistry();
    }

    public static Service getService() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", "test");
        return SimpleWebApplicationServiceImpl.createServiceFrom(request);
    }

    /**
     * Method to add a TicketGrantingTicket to the ticket cache. This should add
     * the ticket and return. Failure upon any exception.
     */
    @Test
    public void testAddTicketToCache() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TEST", TestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }

    @Test
    public void testGetNullTicket() {
        try {
            this.ticketRegistry.getTicket(null, TicketGrantingTicket.class);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            fail("Exception caught.  None expected.");
        }
    }

    @Test
    public void testGetNonExistingTicket() {
        try {
            this.ticketRegistry.getTicket("FALALALALALAL", TicketGrantingTicket.class);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            fail("Exception caught.  None expected.");
        }
    }

    @Test
    public void testGetExistingTicketWithProperClass() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TEST", TestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
            this.ticketRegistry.getTicket("TEST", TicketGrantingTicket.class);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }

    @Test
    public void testGetExistingTicketWithInproperClass() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TEST", TestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
            this.ticketRegistry.getTicket("TEST", ServiceTicket.class);
        } catch (final ClassCastException e) {
            return;
        }
        fail("ClassCastException expected.");
    }

    @Test
    public void testGetNullTicketWithoutClass() {
        try {
            this.ticketRegistry.getTicket(null);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            fail("Exception caught.  None expected.");
        }
    }

    @Test
    public void testGetNonExistingTicketWithoutClass() {
        try {
            this.ticketRegistry.getTicket("FALALALALALAL");
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            fail("Exception caught.  None expected.");
        }
    }

    @Test
    public void testGetExistingTicket() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TEST", TestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
            this.ticketRegistry.getTicket("TEST");
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }

    @Test
    public void testDeleteExistingTicket() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TEST", TestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
            assertTrue("Ticket was not deleted.", this.ticketRegistry.deleteTicket("TEST"));
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }

    @Test
    public void testDeleteNonExistingTicket() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TEST", TestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
            assertFalse("Ticket was deleted.", this.ticketRegistry.deleteTicket("TEST1"));
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }

    @Test
    public void testDeleteNullTicket() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TEST", TestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
            assertFalse("Ticket was deleted.", this.ticketRegistry.deleteTicket(null));
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }

    @Test
    public void testGetTicketsIsZero() {
        try {
            final int size = this.ticketRegistry.getTickets().size();
            assertEquals("The size of the empty registry is not zero.", size, 0);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }

    @Test
    public void testGetTicketsFromRegistryEqualToTicketsAdded() {
        final Collection<Ticket> tickets = new ArrayList<Ticket>();

        for (int i = 0; i < TICKETS_IN_REGISTRY; i++) {
            final TicketGrantingTicket ticketGrantingTicket = new TicketGrantingTicketImpl("TEST" + i,
                    TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
            final ServiceTicket st = ticketGrantingTicket.grantServiceTicket("tests" + i, getService(),
                    new NeverExpiresExpirationPolicy(), false);
            tickets.add(ticketGrantingTicket);
            tickets.add(st);
            this.ticketRegistry.addTicket(ticketGrantingTicket);
            this.ticketRegistry.addTicket(st);
        }

        try {
            Collection<Ticket> ticketRegistryTickets = this.ticketRegistry.getTickets();
            assertEquals("The size of the registry is not the same as the collection.", ticketRegistryTickets.size(),
                    tickets.size());

            for (final Ticket ticket : tickets) {
                if (!ticketRegistryTickets.contains(ticket)) {
                    fail("Ticket was added to registry but was not found in retrieval of collection of all tickets.");
                }
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Cleaning ticket registry to start afresh, after newing up the instance.
     * Leftover items from the cache interfere with the correctness of tests.
     * Resetting the registry instance back to its default empty state allows each
     * test to run an isolated mode independent of the previous state of either cache.
     */
    private void initTicketRegistry() {
        final Iterator<Ticket> it = this.ticketRegistry.getTickets().iterator();

        while (it.hasNext()) {
            this.ticketRegistry.deleteTicket(it.next().getId());
        }
    }
}
