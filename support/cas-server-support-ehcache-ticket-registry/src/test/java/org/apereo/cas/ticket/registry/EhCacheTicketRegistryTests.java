package org.apereo.cas.ticket.registry;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.TestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.EhcacheTicketRegistryConfiguration;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Unit test for {@link EhCacheTicketRegistry}.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {EhcacheTicketRegistryConfiguration.class, RefreshAutoConfiguration.class})
@ContextConfiguration(locations = "classpath:ticketRegistry.xml")
public class EhCacheTicketRegistryTests {

    private static final int TICKETS_IN_REGISTRY = 10;

    private transient Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Before
    public void setUp() throws Exception {
        initTicketRegistry();
    }

    /**
     * Method to add a TicketGrantingTicket to the ticket cache. This should add
     * the ticket and return. Failure upon any exception.
     */
    @Test
    public void verifyAddTicketToCache() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TEST", TestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }

    @Test
    public void verifyGetNullTicket() {
        try {
            this.ticketRegistry.getTicket(null, TicketGrantingTicket.class);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            fail("Exception caught.  None expected.");
        }
    }

    @Test
    public void verifyGetNonExistingTicket() {
        try {
            this.ticketRegistry.getTicket("FALALALALALAL", TicketGrantingTicket.class);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            fail("Exception caught.  None expected.");
        }
    }

    @Test
    public void verifyGetExistingTicketWithProperClass() {
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
    public void verifyGetExistingTicketWithInproperClass() {
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
    public void verifyGetNullTicketWithoutClass() {
        try {
            this.ticketRegistry.getTicket(null);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            fail("Exception caught.  None expected.");
        }
    }

    @Test
    public void verifyGetNonExistingTicketWithoutClass() {
        try {
            this.ticketRegistry.getTicket("FALALALALALAL");
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            fail("Exception caught.  None expected.");
        }
    }

    @Test
    public void verifyGetExistingTicket() {
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
    public void verifyDeleteExistingTicket() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TEST", TestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
            assertSame(1, this.ticketRegistry.deleteTicket("TEST"));
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }

    @Test
    public void verifyDeleteNonExistingTicket() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TEST", TestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
            assertSame(0, this.ticketRegistry.deleteTicket("DOESNOTEXIST"));
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }

    @Test
    public void verifyDeleteNullTicket() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TEST", TestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
            assertFalse("Ticket was deleted.", this.ticketRegistry.deleteTicket(null) == 1);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }

    @Test
    public void verifyGetTicketsIsZero() {
        try {
            final int size = this.ticketRegistry.getTickets().size();
            assertEquals("The size of the empty registry is not zero.", size, 0);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }

    @Test
    public void verifyGetTicketsFromRegistryEqualToTicketsAdded() {
        final Collection<Ticket> tickets = new ArrayList<>();

        for (int i = 0; i < TICKETS_IN_REGISTRY; i++) {
            final TicketGrantingTicket ticketGrantingTicket = new TicketGrantingTicketImpl("TEST" + i,
                    TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
            final ServiceTicket st = ticketGrantingTicket.grantServiceTicket("tests" + i,
                    org.apereo.cas.services.TestUtils.getService(),
                    new NeverExpiresExpirationPolicy(), false, true);
            tickets.add(ticketGrantingTicket);
            tickets.add(st);
            this.ticketRegistry.addTicket(ticketGrantingTicket);
            this.ticketRegistry.addTicket(st);
        }

        try {
            final Collection<Ticket> ticketRegistryTickets = this.ticketRegistry.getTickets();
            assertEquals("The size of the registry is not the same as the collection.", ticketRegistryTickets.size(),
                    tickets.size());

            tickets.stream().filter(ticket -> !ticketRegistryTickets.contains(ticket))
                    .forEach(ticket -> fail("Ticket was added to registry but was not found in retrieval of collection of all tickets."));
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }

    @Test
    public void verifyDeleteTicketWithChildren() {
        this.ticketRegistry.addTicket(new TicketGrantingTicketImpl(
                "TGT", TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy()));
        final TicketGrantingTicket tgt = this.ticketRegistry.getTicket(
                "TGT", TicketGrantingTicket.class);

        final Service service = org.apereo.cas.services.TestUtils.getService("TGT_DELETE_TEST");

        final ServiceTicket st1 = tgt.grantServiceTicket(
                "ST1", service, new NeverExpiresExpirationPolicy(), false, false);
        final ServiceTicket st2 = tgt.grantServiceTicket(
                "ST2", service, new NeverExpiresExpirationPolicy(), false, false);
        final ServiceTicket st3 = tgt.grantServiceTicket(
                "ST3", service, new NeverExpiresExpirationPolicy(), false, false);

        this.ticketRegistry.addTicket(st1);
        this.ticketRegistry.addTicket(st2);
        this.ticketRegistry.addTicket(st3);

        assertNotNull(this.ticketRegistry.getTicket("TGT", TicketGrantingTicket.class));
        assertNotNull(this.ticketRegistry.getTicket("ST1", ServiceTicket.class));
        assertNotNull(this.ticketRegistry.getTicket("ST2", ServiceTicket.class));
        assertNotNull(this.ticketRegistry.getTicket("ST3", ServiceTicket.class));

        assertSame(4, this.ticketRegistry.deleteTicket(tgt.getId()));

        assertNull(this.ticketRegistry.getTicket("TGT", TicketGrantingTicket.class));
        assertNull(this.ticketRegistry.getTicket("ST1", ServiceTicket.class));
        assertNull(this.ticketRegistry.getTicket("ST2", ServiceTicket.class));
        assertNull(this.ticketRegistry.getTicket("ST3", ServiceTicket.class));
    }

    @Test
    public void verifyDeleteTicketWithPGT() {
        final Authentication a = TestUtils.getAuthentication();
        this.ticketRegistry.addTicket(new TicketGrantingTicketImpl(
                "TGT", a, new NeverExpiresExpirationPolicy()));
        final TicketGrantingTicket tgt = this.ticketRegistry.getTicket(
                "TGT", TicketGrantingTicket.class);

        final Service service = org.apereo.cas.services.TestUtils.getService("TGT_DELETE_TEST");

        final ServiceTicket st1 = tgt.grantServiceTicket(
                "ST1", service, new NeverExpiresExpirationPolicy(), false, true);

        this.ticketRegistry.addTicket(st1);

        assertNotNull(this.ticketRegistry.getTicket("TGT", TicketGrantingTicket.class));
        assertNotNull(this.ticketRegistry.getTicket("ST1", ServiceTicket.class));

        final ProxyGrantingTicket pgt = st1.grantProxyGrantingTicket("PGT-1", a, new NeverExpiresExpirationPolicy());
        assertEquals(a, pgt.getAuthentication());

        this.ticketRegistry.addTicket(pgt);
        assertSame(3, this.ticketRegistry.deleteTicket(tgt.getId()));

        assertNull(this.ticketRegistry.getTicket("TGT", TicketGrantingTicket.class));
        assertNull(this.ticketRegistry.getTicket("ST1", ServiceTicket.class));
        assertNull(this.ticketRegistry.getTicket("PGT-1", ProxyGrantingTicket.class));
    }

    /**
     * Cleaning ticket registry to start afresh, after newing up the instance.
     * Leftover items from the cache interfere with the correctness of tests.
     * Resetting the registry instance back to its default empty state allows each
     * test to run an isolated mode independent of the previous state of either cache.
     */
    private void initTicketRegistry() {

        for (final Ticket ticket : this.ticketRegistry.getTickets()) {
            this.ticketRegistry.deleteTicket(ticket.getId());
        }
    }
}
