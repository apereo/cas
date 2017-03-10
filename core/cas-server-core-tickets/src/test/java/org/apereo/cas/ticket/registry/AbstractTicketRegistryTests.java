package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceTestUtils;
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
    private static final String EXCEPTION_CAUGHT_NONE_EXPECTED = "Exception caught.  None expected.";
    private static final String CAUGHT_AN_EXCEPTION_BUT_WAS_NOT_EXPECTED = "Caught an exception. But no exception should have been thrown: ";

    private TicketRegistry ticketRegistry;

    @Before
    public void setUp() throws Exception {
        this.ticketRegistry = this.getNewTicketRegistry();
        if (ticketRegistry != null) {
            this.ticketRegistry.deleteAll();
        }
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
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl(TicketGrantingTicket.PREFIX,
                    CoreAuthenticationTestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
        } catch (final Exception e) {
            fail(EXCEPTION_CAUGHT_NONE_EXPECTED);
        }
    }

    @Test
    public void verifyGetNullTicket() {
        try {
            this.ticketRegistry.getTicket(null, TicketGrantingTicket.class);
        } catch (final Exception e) {
            fail(EXCEPTION_CAUGHT_NONE_EXPECTED);
        }
    }

    @Test
    public void verifyGetNonExistingTicket() {
        try {
            this.ticketRegistry.getTicket("FALALALALALAL", TicketGrantingTicket.class);
        } catch (final Exception e) {
            fail(EXCEPTION_CAUGHT_NONE_EXPECTED);
        }
    }

    @Test
    public void verifyGetExistingTicketWithProperClass() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl(TicketGrantingTicket.PREFIX,
                    CoreAuthenticationTestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
            this.ticketRegistry.getTicket(TicketGrantingTicket.PREFIX, TicketGrantingTicket.class);
        } catch (final Exception e) {
            fail(EXCEPTION_CAUGHT_NONE_EXPECTED);
        }
    }

    @Test
    public void verifyGetExistingTicketWithImproperClass() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl(TicketGrantingTicket.PREFIX,
                    CoreAuthenticationTestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
            this.ticketRegistry.getTicket(TicketGrantingTicket.PREFIX, ServiceTicket.class);
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
            fail(EXCEPTION_CAUGHT_NONE_EXPECTED);
        }
    }

    @Test
    public void verifyGetNonExistingTicketWithoutClass() {
        try {
            this.ticketRegistry.getTicket("FALALALALALAL");
        } catch (final Exception e) {
            fail(EXCEPTION_CAUGHT_NONE_EXPECTED);
        }
    }

    @Test
    public void verifyGetExistingTicket() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl(TicketGrantingTicket.PREFIX,
                    CoreAuthenticationTestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
            this.ticketRegistry.getTicket(TicketGrantingTicket.PREFIX);
        } catch (final Exception e) {
            fail(CAUGHT_AN_EXCEPTION_BUT_WAS_NOT_EXPECTED + e.getMessage());
        }
    }

    @Test
    public void verifyDeleteAllExistingTickets() {
        try {
            for (int i = 0; i < TICKETS_IN_REGISTRY; i++) {
                this.ticketRegistry.addTicket(new TicketGrantingTicketImpl(TicketGrantingTicket.PREFIX + i,
                        CoreAuthenticationTestUtils.getAuthentication(),
                        new NeverExpiresExpirationPolicy()));
            }
            assertEquals(TICKETS_IN_REGISTRY, this.ticketRegistry.deleteAll());
        } catch (final Exception e) {
            fail(CAUGHT_AN_EXCEPTION_BUT_WAS_NOT_EXPECTED + e.getMessage());
        }
    }

    @Test
    public void verifyDeleteExistingTicket() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl(TicketGrantingTicket.PREFIX,
                    CoreAuthenticationTestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
            assertSame(1, this.ticketRegistry.deleteTicket(TicketGrantingTicket.PREFIX));
        } catch (final Exception e) {
            fail(CAUGHT_AN_EXCEPTION_BUT_WAS_NOT_EXPECTED + e.getMessage());
        }
    }

    @Test
    public void verifyDeleteNonExistingTicket() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl(TicketGrantingTicket.PREFIX,
                    CoreAuthenticationTestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
            assertSame(0, this.ticketRegistry.deleteTicket(TicketGrantingTicket.PREFIX + "1"));
        } catch (final Exception e) {
            fail(EXCEPTION_CAUGHT_NONE_EXPECTED);
        }
    }

    @Test
    public void verifyDeleteNullTicket() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl(TicketGrantingTicket.PREFIX,
                    CoreAuthenticationTestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
            assertFalse("Ticket was deleted.", this.ticketRegistry.deleteTicket(null) == 1);
        } catch (final Exception e) {
            fail(EXCEPTION_CAUGHT_NONE_EXPECTED);
        }
    }

    @Test
    public void verifyGetTicketsIsZero() {
        try {
            assertEquals("The size of the empty registry is not zero.", 0, this.ticketRegistry.getTickets().size());
        } catch (final Exception e) {
            fail(EXCEPTION_CAUGHT_NONE_EXPECTED);
        }
    }

    @Test
    public void verifyGetTicketsFromRegistryEqualToTicketsAdded() {
        final Collection<Ticket> tickets = new ArrayList<>();

        for (int i = 0; i < TICKETS_IN_REGISTRY; i++) {
            final TicketGrantingTicket ticketGrantingTicket = new TicketGrantingTicketImpl(TicketGrantingTicket.PREFIX + i,
                    CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
            final ServiceTicket st = ticketGrantingTicket.grantServiceTicket("ST" + i,
                    RegisteredServiceTestUtils.getService(),
                    new NeverExpiresExpirationPolicy(), false, true);
            tickets.add(ticketGrantingTicket);
            tickets.add(st);
            this.ticketRegistry.addTicket(ticketGrantingTicket);
            this.ticketRegistry.addTicket(st);
        }

        try {
            final Collection<Ticket> ticketRegistryTickets = this.ticketRegistry.getTickets();
            assertEquals("The size of the registry is not the same as the collection.",
                    tickets.size(), ticketRegistryTickets.size());


            tickets.stream().filter(ticket -> !ticketRegistryTickets.contains(ticket))
                    .forEach(ticket -> fail("Ticket was added to registry but was not found in retrieval of collection of all tickets."));
        } catch (final Exception e) {
            fail(EXCEPTION_CAUGHT_NONE_EXPECTED);
        }
    }

    @Test
    public void verifyDeleteTicketWithChildren() {
        try {
            this.ticketRegistry.addTicket(new TicketGrantingTicketImpl(TicketGrantingTicket.PREFIX + "1", CoreAuthenticationTestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
            final TicketGrantingTicket tgt = this.ticketRegistry.getTicket(TicketGrantingTicket.PREFIX + "1", TicketGrantingTicket.class);

            final Service service = RegisteredServiceTestUtils.getService("TGT_DELETE_TEST");

            final ServiceTicket st1 = tgt.grantServiceTicket(
                    "ST11", service, new NeverExpiresExpirationPolicy(), false, false);
            final ServiceTicket st2 = tgt.grantServiceTicket(
                    "ST21", service, new NeverExpiresExpirationPolicy(), false, false);
            final ServiceTicket st3 = tgt.grantServiceTicket(
                    "ST31", service, new NeverExpiresExpirationPolicy(), false, false);

            this.ticketRegistry.addTicket(st1);
            this.ticketRegistry.addTicket(st2);
            this.ticketRegistry.addTicket(st3);

            assertNotNull(this.ticketRegistry.getTicket(TicketGrantingTicket.PREFIX + "1", TicketGrantingTicket.class));
            assertNotNull(this.ticketRegistry.getTicket("ST11", ServiceTicket.class));
            assertNotNull(this.ticketRegistry.getTicket("ST21", ServiceTicket.class));
            assertNotNull(this.ticketRegistry.getTicket("ST31", ServiceTicket.class));

            this.ticketRegistry.updateTicket(tgt);
            assertSame(4, this.ticketRegistry.deleteTicket(tgt.getId()));

            assertNull(this.ticketRegistry.getTicket(TicketGrantingTicket.PREFIX + "1", TicketGrantingTicket.class));
            assertNull(this.ticketRegistry.getTicket("ST11", ServiceTicket.class));
            assertNull(this.ticketRegistry.getTicket("ST21", ServiceTicket.class));
            assertNull(this.ticketRegistry.getTicket("ST31", ServiceTicket.class));
        } catch (final Exception e) {
            fail(CAUGHT_AN_EXCEPTION_BUT_WAS_NOT_EXPECTED + e.getMessage());
        }
    }

}
