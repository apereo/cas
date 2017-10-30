package org.apereo.cas.ticket.registry;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.support.AlwaysExpiresExpirationPolicy;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.apereo.cas.util.cipher.NoOpCipherExecutor;
import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.test.util.AopTestUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public abstract class AbstractTicketRegistryTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    private static final String TGT_ID = "TGT";
    private static final String ST_1_ID = "ST1";
    private static final String PGT_1_ID = "PGT-1";

    private static final int TICKETS_IN_REGISTRY = 10;
    private static final String EXCEPTION_CAUGHT_NONE_EXPECTED = "Exception caught.  None expected.";
    private static final String CAUGHT_AN_EXCEPTION_BUT_WAS_NOT_EXPECTED = "Caught an exception. But no exception should have been thrown: ";

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    private final boolean useEncryption;
    private TicketRegistry ticketRegistry;

    public AbstractTicketRegistryTests(final boolean useEncryption) {
        this.useEncryption = useEncryption;
    }

    @Before
    public void setUp() throws Exception {
        this.ticketRegistry = this.getNewTicketRegistry();
        if (ticketRegistry != null) {
            this.ticketRegistry.deleteAll();
            setUpEncryption();
        }
    }
    
    private void setUpEncryption() {
        final AbstractTicketRegistry registry = AopTestUtils.getTargetObject(this.ticketRegistry);
        if (this.useEncryption) {
            final CipherExecutor cipher = Beans.newTicketRegistryCipherExecutor(
                    new EncryptionRandomizedSigningJwtCryptographyProperties(), "[tests]");
            registry.setCipherExecutor(cipher);
        } else {
            registry.setCipherExecutor(NoOpCipherExecutor.getInstance());
        }
    }

    /**
     * Abstract method to retrieve a new ticket registry. Implementing classes
     * return the TicketRegistry they wish to test.
     *
     * @return the TicketRegistry we wish to test
     */
    public abstract TicketRegistry getNewTicketRegistry();

    /**
     * Determine whether the tested registry is able to iterate its tickets.
     */
    protected boolean isIterableRegistry() {
        return true;
    }

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
            assertNull(this.ticketRegistry.getTicket(TicketGrantingTicket.PREFIX, ServiceTicket.class));
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
    public void verifyAddAndUpdateTicket() {
        try {
            TicketGrantingTicket tgt = new TicketGrantingTicketImpl(
                    TicketGrantingTicket.PREFIX,
                    CoreAuthenticationTestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy());
            this.ticketRegistry.addTicket(tgt);

            tgt = this.ticketRegistry.getTicket(tgt.getId(), TicketGrantingTicket.class);
            assertTrue(tgt.getServices().isEmpty());

            tgt.grantServiceTicket("ST1", RegisteredServiceTestUtils.getService("TGT_UPDATE_TEST"),
                    new NeverExpiresExpirationPolicy(), false, false);
            this.ticketRegistry.updateTicket(tgt);

            tgt = this.ticketRegistry.getTicket(tgt.getId(), TicketGrantingTicket.class);
            assertEquals(Collections.singleton("ST1"), tgt.getServices().keySet());
        } catch (final Exception e) {
            fail(CAUGHT_AN_EXCEPTION_BUT_WAS_NOT_EXPECTED + e.getMessage());
        }
    }

    @Test
    public void verifyDeleteAllExistingTickets() {
        Assume.assumeTrue(isIterableRegistry());
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
            assertNull(this.ticketRegistry.getTicket(TicketGrantingTicket.PREFIX));
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
            assertSame(0, this.ticketRegistry.deleteTicket(TicketGrantingTicket.PREFIX + "NON-EXISTING-SUFFIX"));
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
        Assume.assumeTrue(isIterableRegistry());
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
                    .forEach(ticket -> fail("Ticket " + ticket + " was not found in retrieval of collection of all tickets."));
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

    @Test
    public void verifyWriteGetDelete() {
        final Ticket ticket = new TicketGrantingTicketImpl(TicketGrantingTicket.PREFIX,
                CoreAuthenticationTestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        ticketRegistry.addTicket(ticket);
        final Ticket ticketFromRegistry = ticketRegistry.getTicket(TicketGrantingTicket.PREFIX);
        assertNotNull(ticketFromRegistry);
        assertEquals(TicketGrantingTicket.PREFIX, ticketFromRegistry.getId());
        ticketRegistry.deleteTicket(TicketGrantingTicket.PREFIX);
        assertNull(ticketRegistry.getTicket(TicketGrantingTicket.PREFIX));
    }

    @Test
    public void verifyExpiration() {
        final String id = "ST-1234567890ABCDEFGHIJKL-exp1";
        final MockServiceTicket ticket = new MockServiceTicket(id, RegisteredServiceTestUtils.getService(), new MockTicketGrantingTicket("test"));
        ticket.setExpiration(new AlwaysExpiresExpirationPolicy());
        ticketRegistry.addTicket(ticket);
        assertNull(ticketRegistry.getTicket(id, ServiceTicket.class));
    }

    @Test
    public void verifyDeleteTicketWithPGT() {
        final Authentication a = CoreAuthenticationTestUtils.getAuthentication();
        this.ticketRegistry.addTicket(new TicketGrantingTicketImpl(TGT_ID, a, new NeverExpiresExpirationPolicy()));
        final TicketGrantingTicket tgt = this.ticketRegistry.getTicket(TGT_ID, TicketGrantingTicket.class);

        final Service service = RegisteredServiceTestUtils.getService("TGT_DELETE_TEST");

        final ServiceTicket st1 = tgt.grantServiceTicket(ST_1_ID, service, new NeverExpiresExpirationPolicy(), false, true);
        this.ticketRegistry.addTicket(st1);
        this.ticketRegistry.updateTicket(tgt);

        assertNotNull(this.ticketRegistry.getTicket(TGT_ID, TicketGrantingTicket.class));
        assertNotNull(this.ticketRegistry.getTicket(ST_1_ID, ServiceTicket.class));

        final ProxyGrantingTicket pgt = st1.grantProxyGrantingTicket(PGT_1_ID, a, new NeverExpiresExpirationPolicy());
        this.ticketRegistry.addTicket(pgt);
        this.ticketRegistry.updateTicket(tgt);
        this.ticketRegistry.updateTicket(st1);
        assertEquals(pgt.getGrantingTicket(), tgt);
        assertNotNull(this.ticketRegistry.getTicket(PGT_1_ID, ProxyGrantingTicket.class));
        assertEquals(a, pgt.getAuthentication());
        assertNotNull(this.ticketRegistry.getTicket(ST_1_ID, ServiceTicket.class));

        assertTrue(this.ticketRegistry.deleteTicket(tgt.getId()) > 0);

        assertNull(this.ticketRegistry.getTicket(TGT_ID, TicketGrantingTicket.class));
        assertNull(this.ticketRegistry.getTicket(ST_1_ID, ServiceTicket.class));
        assertNull(this.ticketRegistry.getTicket(PGT_1_ID, ProxyGrantingTicket.class));
    }

    @Test
    public void verifyDeleteTicketsWithMultiplePGTs() {
        final Authentication a = CoreAuthenticationTestUtils.getAuthentication();
        this.ticketRegistry.addTicket(new TicketGrantingTicketImpl(TGT_ID, a, new NeverExpiresExpirationPolicy()));
        final TicketGrantingTicket tgt = this.ticketRegistry.getTicket(TGT_ID, TicketGrantingTicket.class);

        final Service service = RegisteredServiceTestUtils.getService("TGT_DELETE_TEST");
        IntStream.range(1, 5).forEach(i -> {
            final ServiceTicket st = tgt.grantServiceTicket(ST_1_ID + "-" + i, service,
                    new NeverExpiresExpirationPolicy(), false, true);
            this.ticketRegistry.addTicket(st);
            this.ticketRegistry.updateTicket(tgt);

            final ProxyGrantingTicket pgt = st.grantProxyGrantingTicket(PGT_1_ID + "-" + i, a, new NeverExpiresExpirationPolicy());
            this.ticketRegistry.addTicket(pgt);
            this.ticketRegistry.updateTicket(tgt);
            this.ticketRegistry.updateTicket(st);
        });

        final int c = this.ticketRegistry.deleteTicket(TGT_ID);
        assertEquals(c, 6);
    }
}
