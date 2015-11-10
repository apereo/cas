package org.jasig.cas.ticket.registry;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;

import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.jasig.cas.authentication.principal.Service;
import org.junit.Before;
import org.junit.Test;

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
                    org.jasig.cas.authentication.TestUtils.getAuthentication(),
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
                    org.jasig.cas.authentication.TestUtils.getAuthentication(),
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
                    org.jasig.cas.authentication.TestUtils.getAuthentication(),
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
                    org.jasig.cas.authentication.TestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
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
                    org.jasig.cas.authentication.TestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
            assertTrue("Ticket was not deleted.", this.ticketRegistry.deleteTicket("TEST"));
        } catch (final Exception e) {
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }

    @Test
    public void verifyDeleteNonExistingTicket() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TEST",
                    org.jasig.cas.authentication.TestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
            assertFalse("Ticket was deleted.", this.ticketRegistry.deleteTicket("TEST1"));
        } catch (final Exception e) {
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }

    @Test
    public void verifyDeleteNullTicket() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl("TEST",
                    org.jasig.cas.authentication.TestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
            assertFalse("Ticket was deleted.", this.ticketRegistry.deleteTicket(null));
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
                    org.jasig.cas.authentication.TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
            final ServiceTicket st = ticketGrantingTicket.grantServiceTicket("tests" + i,
                    org.jasig.cas.services.TestUtils.getService(),
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

            for (final Ticket ticket : tickets) {
                if (!ticketRegistryTickets.contains(ticket)) {
                    fail("Ticket was added to registry but was not found in retrieval of collection of all tickets.");
                }
            }
        } catch (final Exception e) {
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }

    @Test
    public void verifyDeleteTicketWithChildren() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl(
                    "TGT", org.jasig.cas.authentication.TestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
            final TicketGrantingTicket tgt = this.ticketRegistry.getTicket(
                    "TGT", TicketGrantingTicket.class);

            final Service service = org.jasig.cas.services.TestUtils.getService("TGT_DELETE_TEST");

            final ServiceTicket st1 = tgt.grantServiceTicket(
                    "ST1", service, new NeverExpiresExpirationPolicy(), true, false);
            final ServiceTicket st2 = tgt.grantServiceTicket(
                    "ST2", service, new NeverExpiresExpirationPolicy(), true, false);
            final ServiceTicket st3 = tgt.grantServiceTicket(
                    "ST3", service, new NeverExpiresExpirationPolicy(), true, false);

            this.ticketRegistry.addTicket(st1);
            this.ticketRegistry.addTicket(st2);
            this.ticketRegistry.addTicket(st3);

            assertNotNull(this.ticketRegistry.getTicket("TGT", TicketGrantingTicket.class));
            assertNotNull(this.ticketRegistry.getTicket("ST1", ServiceTicket.class));
            assertNotNull(this.ticketRegistry.getTicket("ST2", ServiceTicket.class));
            assertNotNull(this.ticketRegistry.getTicket("ST3", ServiceTicket.class));

            this.ticketRegistry.deleteTicket(tgt.getId());

            assertNull(this.ticketRegistry.getTicket("TGT", TicketGrantingTicket.class));
            assertNull(this.ticketRegistry.getTicket("ST1", ServiceTicket.class));
            assertNull(this.ticketRegistry.getTicket("ST2", ServiceTicket.class));
            assertNull(this.ticketRegistry.getTicket("ST3", ServiceTicket.class));
        } catch (final Exception e) {
            fail("Caught an exception. But no exception should have been thrown.");
        }
    }

}
