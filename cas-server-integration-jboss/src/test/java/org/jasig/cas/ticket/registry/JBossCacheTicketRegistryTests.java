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

import org.apache.commons.io.IOUtils;
import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.jboss.cache.Cache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * @deprecated As of 4.1 the Jboss cache integration module is no longer supported.
 * Please use other means of confguring your distributed ticket registry, such as
 * ehcache or memcached integrations with CAS.
 *
 * <p>Test case to test the DefaultTicketRegistry based on test cases to test all
 * Ticket Registries.
 *
 * either ehcache or memcached modules.
 * @author Scott Battaglia
 * @author Marc-Antoine Garrigue
 * @since 3.0.0
 */
@Deprecated
public final class JBossCacheTicketRegistryTests {

    private static final String APPLICATION_CONTEXT_FILE_NAME = "jbossTestContext.xml";

    private static final String APPLICATION_CONTEXT_CACHE_BEAN_NAME = "ticketRegistry";

    private static final int TICKETS_IN_REGISTRY = 10;

    private JBossCacheTicketRegistry registry;

    private Cache<String, Ticket> treeCache;

    private TicketRegistry ticketRegistry;

    private ClassPathXmlApplicationContext context;
    
    @Before
    public void setUp() throws Exception {
        this.ticketRegistry = this.getNewTicketRegistry();
    }

    @After
    public void shutdown() {
        IOUtils.closeQuietly(this.context);
    }
    
    public TicketRegistry getNewTicketRegistry() throws Exception {
        this.context = new ClassPathXmlApplicationContext(
                APPLICATION_CONTEXT_FILE_NAME);
        this.registry = (JBossCacheTicketRegistry) context
                .getBean(APPLICATION_CONTEXT_CACHE_BEAN_NAME);

        this.treeCache = (Cache<String, Ticket>) context.getBean("cache");
        this.treeCache.removeNode("/ticket");
        return this.registry;
    }

    /**
     * Method to add a TicketGrantingTicket to the ticket cache. This should add
     * the ticket and return. Failure upon any exception.
     */
    @Test
    public void verifyAddTicketToCache() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TEST",
                    TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy()));
        } catch (final Exception e) {
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }

    @Test
    public void verifyGetNullTicket() {
        try {
            this.ticketRegistry.getTicket(null, TicketGrantingTicket.class);
        } catch (final Exception e) {
            fail("Exception caught.  None expected.");
        }
    }

    @Test
    public void verifyGetNonExistingTicket() {
        try {
            this.ticketRegistry.getTicket("FALALALALALAL",
                    TicketGrantingTicket.class);
        } catch (final Exception e) {
            fail("Exception caught.  None expected.");
        }
    }

    @Test
    public void verifyGetExistingTicketWithProperClass() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TEST",
                    TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy()));
            this.ticketRegistry.getTicket("TEST", TicketGrantingTicket.class);
        } catch (final Exception e) {
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }

    @Test
    public void verifyGetExistingTicketWithInproperClass() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TEST",
                    TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy()));
            this.ticketRegistry.getTicket("TEST", ServiceTicket.class);
        } catch (final ClassCastException e) {
            return;
        }
        fail("ClassCastException expected.");
    }

    @Test
    public void verifyGetNullTicketWithoutClass() {
        try {
            this.ticketRegistry.getTicket(null);
        } catch (final Exception e) {
            fail("Exception caught.  None expected.");
        }
    }

    @Test
    public void verifyGetNonExistingTicketWithoutClass() {
        try {
            this.ticketRegistry.getTicket("FALALALALALAL");
        } catch (final Exception e) {
            fail("Exception caught.  None expected.");
        }
    }

    @Test
    public void verifyGetExistingTicket() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TEST",
                    TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy()));
            this.ticketRegistry.getTicket("TEST");
        } catch (final Exception e) {
            e.printStackTrace();
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }

    @Test
    public void verifyDeleteExistingTicket() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TEST",
                    TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy()));
            assertTrue("Ticket was not deleted.", this.ticketRegistry
                    .deleteTicket("TEST"));
        } catch (final Exception e) {
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }

    @Test
    public void verifyDeleteNonExistingTicket() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TEST",
                    TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy()));
            assertFalse("Ticket was deleted.", this.ticketRegistry
                    .deleteTicket("TEST1"));
        } catch (final Exception e) {
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }

    @Test
    public void verifyDeleteNullTicket() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TEST",
                    TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy()));
            assertFalse("Ticket was deleted.", this.ticketRegistry
                    .deleteTicket(null));
        } catch (final Exception e) {
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }

    @Test
    public void verifyGetTicketsIsZero() {
        try {
            assertEquals("The size of the empty registry is not zero.",
                    this.ticketRegistry.getTickets().size(), 0);
        } catch (final Exception e) {
            e.printStackTrace();
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }

    @Test
    public void verifyGetTicketsFromRegistryEqualToTicketsAdded() {
        final Collection<Ticket> tickets = new ArrayList<>();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", "test");

        for (int i = 0; i < TICKETS_IN_REGISTRY; i++) {
            final TicketGrantingTicket ticketGrantingTicket = new TicketGrantingTicketImpl(
                    "TEST" + i, TestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy());
            final ServiceTicket st = ticketGrantingTicket.grantServiceTicket(
                    "tests" + i, SimpleWebApplicationServiceImpl.createServiceFrom(request),
                    new NeverExpiresExpirationPolicy(), false);
            tickets.add(ticketGrantingTicket);
            tickets.add(st);
            this.ticketRegistry.addTicket(ticketGrantingTicket);
            this.ticketRegistry.addTicket(st);
        }

        try {
            final Collection<Ticket> ticketRegistryTickets = this.ticketRegistry.getTickets();
            assertEquals(
                    "The size of the registry is not the same as the collection.",
                    ticketRegistryTickets.size(), tickets.size());

            for (final Ticket ticket : tickets) {
                if (!ticketRegistryTickets.contains(ticket)) {
                    fail("Ticket was added to registry but was not found in retrieval of collection of all tickets.");
                }
            }
        } catch (final Exception e) {
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }
}
