package org.apereo.cas.ticket.registry;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.AbstractTicket;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.support.AlwaysExpiresExpirationPolicy;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.ProxyGrantingTicketIdGenerator;
import org.apereo.cas.util.ServiceTicketIdGenerator;
import org.apereo.cas.util.TicketGrantingTicketIdGenerator;
import org.apereo.cas.util.junit.ConditionalIgnoreRule;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.AopTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * This is {@link BaseTicketRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public abstract class BaseTicketRegistryTests {

    private static final int TICKETS_IN_REGISTRY = 10;
    private static final String EXCEPTION_CAUGHT_NONE_EXPECTED = "Exception caught. None expected.";
    private static final String CAUGHT_AN_EXCEPTION_BUT_WAS_NOT_EXPECTED = "Caught an exception. But no exception should have been thrown: ";

    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();

    private String ticketGrantingTicketId;
    private String serviceTicketId;
    private String proxyGrantingTicketId;

    private final boolean useEncryption;

    private TicketRegistry ticketRegistry;

    public BaseTicketRegistryTests(final boolean useEncryption) {
        this.useEncryption = useEncryption;
    }

    @BeforeEach
    public void initialize() {
        this.ticketGrantingTicketId = new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY)
            .getNewTicketId(TicketGrantingTicket.PREFIX);
        this.serviceTicketId = new ServiceTicketIdGenerator(10, StringUtils.EMPTY)
            .getNewTicketId(ServiceTicket.PREFIX);
        this.proxyGrantingTicketId = new ProxyGrantingTicketIdGenerator(10, StringUtils.EMPTY)
            .getNewTicketId(ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX);

        ticketRegistry = this.getNewTicketRegistry();
        if (ticketRegistry != null) {
            ticketRegistry.deleteAll();
            setUpEncryption();
        }
    }

    protected abstract TicketRegistry getNewTicketRegistry();

    private void setUpEncryption() {
        var registry = (AbstractTicketRegistry) AopTestUtils.getTargetObject(ticketRegistry);
        if (this.useEncryption) {
            val cipher = CoreTicketUtils.newTicketRegistryCipherExecutor(
                new EncryptionRandomizedSigningJwtCryptographyProperties(), "[tests]");
            registry.setCipherExecutor(cipher);
        } else {
            registry.setCipherExecutor(CipherExecutor.noOp());
        }
    }

    /**
     * Determine whether the tested registry is able to iterate its tickets.
     */
    protected boolean isIterableRegistry() {
        return true;
    }

    @Test
    public void verifyAddTicketToCache() {
        try {
            ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
                CoreAuthenticationTestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy()));
        } catch (final Exception e) {
            throw new AssertionError(EXCEPTION_CAUGHT_NONE_EXPECTED, e);
        }
    }

    @Test
    public void verifyGetNullTicket() {
        try {
            ticketRegistry.getTicket(null, TicketGrantingTicket.class);
        } catch (final Exception e) {
            throw new AssertionError(EXCEPTION_CAUGHT_NONE_EXPECTED, e);
        }
    }

    @Test
    public void verifyGetNonExistingTicket() {
        try {
            ticketRegistry.getTicket("FALALALALALAL", TicketGrantingTicket.class);
        } catch (final Exception e) {
            throw new AssertionError(EXCEPTION_CAUGHT_NONE_EXPECTED, e);
        }
    }

    @Test
    public void verifyGetExistingTicketWithProperClass() {
        try {
            ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
                CoreAuthenticationTestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy()));
            ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
        } catch (final Exception e) {
            throw new AssertionError(EXCEPTION_CAUGHT_NONE_EXPECTED, e);
        }
    }

    @Test
    public void verifyGetExistingTicketWithImproperClass() {
        var ticket = (Ticket) null;
        try {
            ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
                CoreAuthenticationTestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy()));
            ticket = ticketRegistry.getTicket(ticketGrantingTicketId, ServiceTicket.class);
            assertNull(ticket);
        } catch (final ClassCastException e) {
            LOGGER.trace("Ticket type does not match the requested type");
        }
    }

    @Test
    public void verifyGetNullTicketWithoutClass() {
        try {
            ticketRegistry.getTicket(null);
        } catch (final Exception e) {
            throw new AssertionError(EXCEPTION_CAUGHT_NONE_EXPECTED, e);
        }
    }

    @Test
    public void verifyGetNonExistingTicketWithoutClass() {
        try {
            ticketRegistry.getTicket("FALALALALALAL");
        } catch (final Exception e) {
            throw new AssertionError(EXCEPTION_CAUGHT_NONE_EXPECTED, e);
        }
    }

    @Test
    public void verifyGetExistingTicket() {
        try {
            ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
                CoreAuthenticationTestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy()));
            ticketRegistry.getTicket(ticketGrantingTicketId);
        } catch (final Exception e) {
            throw new AssertionError(CAUGHT_AN_EXCEPTION_BUT_WAS_NOT_EXPECTED + e.getMessage(), e);
        }
    }

    @Test
    public void verifyAddAndUpdateTicket() {
        try {
            TicketGrantingTicket tgt = new TicketGrantingTicketImpl(
                ticketGrantingTicketId,
                CoreAuthenticationTestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
            ticketRegistry.addTicket(tgt);

            tgt = ticketRegistry.getTicket(tgt.getId(), TicketGrantingTicket.class);
            assertNotNull(tgt);
            assertTrue(tgt.getServices().isEmpty());

            tgt.grantServiceTicket("ST1", RegisteredServiceTestUtils.getService("TGT_UPDATE_TEST"),
                new NeverExpiresExpirationPolicy(), false, false);
            ticketRegistry.updateTicket(tgt);

            tgt = ticketRegistry.getTicket(tgt.getId(), TicketGrantingTicket.class);
            assertEquals(Collections.singleton("ST1"), tgt.getServices().keySet());
        } catch (final Exception e) {
            throw new AssertionError(CAUGHT_AN_EXCEPTION_BUT_WAS_NOT_EXPECTED + e.getMessage(), e);
        }
    }

    @Test
    public void verifyDeleteAllExistingTickets() {
        Assume.assumeTrue(isIterableRegistry());
        try {
            for (var i = 0; i < TICKETS_IN_REGISTRY; i++) {
                ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId + i,
                    CoreAuthenticationTestUtils.getAuthentication(),
                    new NeverExpiresExpirationPolicy()));
            }
            val actual = ticketRegistry.deleteAll();
            assertEquals(TICKETS_IN_REGISTRY, actual);
        } catch (final Exception e) {
            throw new AssertionError(CAUGHT_AN_EXCEPTION_BUT_WAS_NOT_EXPECTED + e.getMessage(), e);
        }
    }

    @Test
    @Transactional
    public void verifyDeleteExistingTicket() {
        try {
            ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
                CoreAuthenticationTestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy()));
            assertSame(1, ticketRegistry.deleteTicket(ticketGrantingTicketId));
            assertNull(ticketRegistry.getTicket(ticketGrantingTicketId));
        } catch (final Exception e) {
            throw new AssertionError(CAUGHT_AN_EXCEPTION_BUT_WAS_NOT_EXPECTED + e.getMessage(), e);
        }
    }

    @Test
    @Transactional
    public void verifyDeleteNonExistingTicket() {
        try {
            ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
                CoreAuthenticationTestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy()));
            assertSame(0, ticketRegistry.deleteTicket(ticketGrantingTicketId + "NON-EXISTING-SUFFIX"));
        } catch (final Exception e) {
            throw new AssertionError(EXCEPTION_CAUGHT_NONE_EXPECTED + e.getMessage(), e);
        }
    }

    @Test
    public void verifyDeleteNullTicket() {
        try {
            ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
                CoreAuthenticationTestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy()));
            assertFalse("Ticket was deleted.", ticketRegistry.deleteTicket(StringUtils.EMPTY) == 1);
        } catch (final Exception e) {
            throw new AssertionError(EXCEPTION_CAUGHT_NONE_EXPECTED + e.getMessage(), e);
        }
    }

    @Test
    public void verifyGetTicketsIsZero() {
        try {
            ticketRegistry.deleteAll();
            assertEquals("The size of the empty registry is not zero.", 0, ticketRegistry.getTickets().size());
        } catch (final Exception e) {
            throw new AssertionError(EXCEPTION_CAUGHT_NONE_EXPECTED + e.getMessage(), e);
        }
    }

    @Test
    public void verifyGetTicketsFromRegistryEqualToTicketsAdded() {
        Assume.assumeTrue(isIterableRegistry());
        val tickets = new ArrayList<Ticket>();

        for (var i = 0; i < TICKETS_IN_REGISTRY; i++) {
            val ticketGrantingTicket = new TicketGrantingTicketImpl(ticketGrantingTicketId + i,
                CoreAuthenticationTestUtils.getAuthentication(), new NeverExpiresExpirationPolicy());
            val st = ticketGrantingTicket.grantServiceTicket("ST" + i,
                RegisteredServiceTestUtils.getService(),
                new NeverExpiresExpirationPolicy(), false, true);
            tickets.add(ticketGrantingTicket);
            tickets.add(st);
            ticketRegistry.addTicket(ticketGrantingTicket);
            ticketRegistry.addTicket(st);
        }

        try {
            val ticketRegistryTickets = ticketRegistry.getTickets();
            assertEquals("The size of the registry is not the same as the collection.",
                tickets.size(), ticketRegistryTickets.size());


            tickets.stream().filter(ticket -> !ticketRegistryTickets.contains(ticket))
                .forEach(ticket -> {
                    throw new AssertionError("Ticket " + ticket + " was not found in retrieval of collection of all tickets.");
                });
        } catch (final Exception e) {
            throw new AssertionError(EXCEPTION_CAUGHT_NONE_EXPECTED + e.getMessage(), e);
        }
    }

    @Test
    @Transactional
    public void verifyDeleteTicketWithChildren() {
        try {
            ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId + '1', CoreAuthenticationTestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy()));
            val tgt = ticketRegistry.getTicket(ticketGrantingTicketId + '1', TicketGrantingTicket.class);

            val service = RegisteredServiceTestUtils.getService("TGT_DELETE_TEST");

            val st1 = tgt.grantServiceTicket(
                "ST11", service, new NeverExpiresExpirationPolicy(), false, false);
            val st2 = tgt.grantServiceTicket(
                "ST21", service, new NeverExpiresExpirationPolicy(), false, false);
            val st3 = tgt.grantServiceTicket(
                "ST31", service, new NeverExpiresExpirationPolicy(), false, false);

            ticketRegistry.addTicket(st1);
            ticketRegistry.addTicket(st2);
            ticketRegistry.addTicket(st3);

            assertNotNull(ticketRegistry.getTicket(ticketGrantingTicketId + '1', TicketGrantingTicket.class));
            assertNotNull(ticketRegistry.getTicket("ST11", ServiceTicket.class));
            assertNotNull(ticketRegistry.getTicket("ST21", ServiceTicket.class));
            assertNotNull(ticketRegistry.getTicket("ST31", ServiceTicket.class));

            ticketRegistry.updateTicket(tgt);
            assertSame(4, ticketRegistry.deleteTicket(tgt.getId()));

            assertNull(ticketRegistry.getTicket(ticketGrantingTicketId + '1', TicketGrantingTicket.class));
            assertNull(ticketRegistry.getTicket("ST11", ServiceTicket.class));
            assertNull(ticketRegistry.getTicket("ST21", ServiceTicket.class));
            assertNull(ticketRegistry.getTicket("ST31", ServiceTicket.class));
        } catch (final Exception e) {
            throw new AssertionError(CAUGHT_AN_EXCEPTION_BUT_WAS_NOT_EXPECTED + e.getMessage(), e);
        }
    }

    @Test
    @Transactional
    public void verifyWriteGetDelete() {
        val ticket = new TicketGrantingTicketImpl(ticketGrantingTicketId,
            CoreAuthenticationTestUtils.getAuthentication(),
            new NeverExpiresExpirationPolicy());
        ticketRegistry.addTicket(ticket);
        val ticketFromRegistry = ticketRegistry.getTicket(ticketGrantingTicketId);
        assertNotNull(ticketFromRegistry);
        assertEquals(ticketGrantingTicketId, ticketFromRegistry.getId());
        ticketRegistry.deleteTicket(ticketGrantingTicketId);
        assertNull(ticketRegistry.getTicket(ticketGrantingTicketId));
    }

    @Test
    public void verifyExpiration() {
        val authn = CoreAuthenticationTestUtils.getAuthentication();
        LOGGER.trace("Adding ticket {}", ticketGrantingTicketId);
        ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId, authn, new NeverExpiresExpirationPolicy()));
        LOGGER.trace("Getting ticket {}", ticketGrantingTicketId);
        val tgt = ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
        assertNotNull("Ticket-granting ticket " + ticketGrantingTicketId + " cannot be fetched", tgt);
        val service = RegisteredServiceTestUtils.getService("TGT_DELETE_TEST");
        LOGGER.trace("Granting service ticket {}", serviceTicketId);
        val ticket = (AbstractTicket) tgt.grantServiceTicket(serviceTicketId, service,
            new NeverExpiresExpirationPolicy(), false, true);
        assertNotNull("Service ticket cannot be null", ticket);
        ticket.setExpirationPolicy(new AlwaysExpiresExpirationPolicy());
        ticketRegistry.addTicket(ticket);
        ticketRegistry.updateTicket(tgt);
        assertNull(ticketRegistry.getTicket(serviceTicketId, ServiceTicket.class));
    }

    @Test
    public void verifyExpiredTicket() {
        val authn = CoreAuthenticationTestUtils.getAuthentication();
        ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId, authn, new AlwaysExpiresExpirationPolicy()));
        var tgt = ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
        assertNull(tgt);
    }


    @Test
    @Transactional
    public void verifyDeleteTicketWithPGT() {
        val a = CoreAuthenticationTestUtils.getAuthentication();
        ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId, a, new NeverExpiresExpirationPolicy()));
        val tgt = ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);

        val service = RegisteredServiceTestUtils.getService("TGT_DELETE_TEST");

        val st1 = tgt.grantServiceTicket(serviceTicketId, service, new NeverExpiresExpirationPolicy(), false, true);
        ticketRegistry.addTicket(st1);
        ticketRegistry.updateTicket(tgt);

        assertNotNull(ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class));
        assertNotNull(ticketRegistry.getTicket(serviceTicketId, ServiceTicket.class));

        val pgt = st1.grantProxyGrantingTicket(proxyGrantingTicketId, a, new NeverExpiresExpirationPolicy());
        ticketRegistry.addTicket(pgt);
        ticketRegistry.updateTicket(tgt);
        ticketRegistry.updateTicket(st1);
        assertEquals(pgt.getTicketGrantingTicket(), tgt);
        assertNotNull(ticketRegistry.getTicket(proxyGrantingTicketId, ProxyGrantingTicket.class));
        assertEquals(a, pgt.getAuthentication());
        assertNotNull(ticketRegistry.getTicket(serviceTicketId, ServiceTicket.class));

        assertTrue(ticketRegistry.deleteTicket(tgt.getId()) > 0);

        assertNull(ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class));
        assertNull(ticketRegistry.getTicket(serviceTicketId, ServiceTicket.class));
        assertNull(ticketRegistry.getTicket(proxyGrantingTicketId, ProxyGrantingTicket.class));
    }

    @Test
    @Transactional
    public void verifyDeleteTicketsWithMultiplePGTs() {
        val a = CoreAuthenticationTestUtils.getAuthentication();
        ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId, a, new NeverExpiresExpirationPolicy()));
        val tgt = ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
        assertNotNull("Ticket-granting ticket must not be null", tgt);
        val service = RegisteredServiceTestUtils.getService("TGT_DELETE_TEST");
        IntStream.range(1, 5).forEach(i -> {
            val st = tgt.grantServiceTicket(serviceTicketId + '-' + i, service,
                new NeverExpiresExpirationPolicy(), false, true);
            ticketRegistry.addTicket(st);
            ticketRegistry.updateTicket(tgt);

            val pgt = st.grantProxyGrantingTicket(proxyGrantingTicketId + '-' + i, a, new NeverExpiresExpirationPolicy());
            ticketRegistry.addTicket(pgt);
            ticketRegistry.updateTicket(tgt);
            ticketRegistry.updateTicket(st);
        });

        val c = ticketRegistry.deleteTicket(ticketGrantingTicketId);
        assertEquals(6, c);
    }
}
