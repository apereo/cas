package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.TestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public abstract class AbstractTicketRegistryTests {

    private static final int TICKETS_IN_REGISTRY = 10;

    private TicketRegistry ticketRegistry;

    @Before
    public void setUp() throws Exception {
        this.ticketRegistry = this.getNewTicketRegistry();
    }

    /**
     * Abstract method to retrieve a new ticket registry. Implementing classes
     * return the TicketRegistry they wish to test.
     *
     * @return the TicketRegistry we wish to test
     * @throws Exception the exception
     */
    public abstract TicketRegistry getNewTicketRegistry() throws Exception;

    /**
     * Method to add a TicketGrantingTicket to the ticket cache. This should add
     * the ticket and return. Failure upon any exception.
     */
    @Test
    public void verifyAddTicketToCache() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TEST",
                    TestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
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
            this.ticketRegistry.getTicket("FALALALALALAL", TicketGrantingTicket.class);
        } catch (final Exception e) {
            fail("Exception caught.  None expected.");
        }
    }

    @Test
    public void verifyGetExistingTicketWithProperClass() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TEST",
                    TestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
            this.ticketRegistry.getTicket("TEST", TicketGrantingTicket.class);
        } catch (final Exception e) {
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }

    @Test
    public void verifyGetExistingTicketWithInproperClass() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TEST",
                    TestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
            this.ticketRegistry.getTicket("TEST", ServiceTicket.class);
        } catch (final ClassCastException e) {
            return;
        }
        fail("ClassCastfinal Exception expected.");
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
                    TestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
            this.ticketRegistry.getTicket("TEST");
        } catch (final Exception e) {
            fail("Caught an exception. But no exception should have been thrown: " + e.getMessage());
        }
    }

    @Test
    public void verifyDeleteExistingTicket() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TEST",
                    TestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
            assertSame(1, this.ticketRegistry.deleteTicket("TEST"));
        } catch (final Exception e) {
            fail("Caught an exception. But no exception should have been thrown: " + e.getMessage());
        }
    }

    @Test
    public void verifyDeleteNonExistingTicket() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TEST",
                    TestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
            assertSame(0, this.ticketRegistry.deleteTicket("TEST1"));
        } catch (final Exception e) {
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }

    @Test
    public void verifyDeleteNullTicket() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TEST",
                    TestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
            assertFalse("Ticket was deleted.", this.ticketRegistry.deleteTicket(null) == 1);
        } catch (final Exception e) {
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }

    @Test
    public void verifyGetTicketsIsZero() {
        try {
            assertEquals("The size of the empty registry is not zero.", this.ticketRegistry.getTickets().size(), 0);
        } catch (final Exception e) {
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
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }

    @Test
    public void verifyDeleteTicketWithChildren() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl(
                    "TGT", TestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
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
        } catch (final Exception e) {
            fail("Caught an exception. But no exception should have been thrown: " + e.getMessage());
        }
    }

}
