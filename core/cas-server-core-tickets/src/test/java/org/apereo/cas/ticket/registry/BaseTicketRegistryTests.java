package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCookieConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreLogoutConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreTicketsSchedulingConfiguration;
import org.apereo.cas.config.CasCoreTicketsSerializationConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasPersonDirectoryStubConfiguration;
import org.apereo.cas.config.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.AbstractTicket;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.ProxyGrantingTicketIssuerTicket;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketImpl;
import org.apereo.cas.ticket.expiration.AlwaysExpiresExpirationPolicy;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.expiration.TicketGrantingTicketExpirationPolicy;
import org.apereo.cas.ticket.expiration.TimeoutExpirationPolicy;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.ProxyGrantingTicketIdGenerator;
import org.apereo.cas.util.ServiceTicketIdGenerator;
import org.apereo.cas.util.TicketGrantingTicketIdGenerator;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.AopTestUtils;
import org.springframework.transaction.annotation.Transactional;
import java.io.Serial;
import java.time.Clock;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

/**
 * This is {@link BaseTicketRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@SpringBootTest(classes = BaseTicketRegistryTests.SharedTestConfiguration.class,
    properties = {
        "cas.ticket.tgt.core.only-track-most-recent-session=false",
        "cas.ticket.registry.cleaner.schedule.enabled=false"
    })
public abstract class BaseTicketRegistryTests {

    private static final int TICKETS_IN_REGISTRY = 1;

    private static final String TICKET_SHOULD_BE_NULL_USE_ENCRYPTION = "Ticket should be null. useEncryption[";

    private static final TicketGrantingTicketIdGenerator TICKET_GRANTING_TICKET_ID_GENERATOR =
        new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY);

    @Autowired
    @Qualifier(TicketFactory.BEAN_NAME)
    protected TicketFactory ticketFactory;

    @Autowired
    @Qualifier(TicketTrackingPolicy.BEAN_NAME_SERVICE_TICKET_TRACKING)
    protected TicketTrackingPolicy serviceTicketSessionTrackingPolicy;

    @Autowired
    @Qualifier(TicketCatalog.BEAN_NAME)
    protected TicketCatalog ticketCatalog;

    @Autowired
    @Qualifier(TicketSerializationManager.BEAN_NAME)
    protected TicketSerializationManager ticketSerializationManager;
    
    protected boolean useEncryption;

    protected String ticketGrantingTicketId;

    protected String serviceTicketId;

    protected String transientSessionTicketId;

    protected String proxyGrantingTicketId;

    private TicketRegistry ticketRegistry;

    protected static ExpirationPolicyBuilder neverExpiresExpirationPolicyBuilder() {
        return new ExpirationPolicyBuilder() {
            @Serial
            private static final long serialVersionUID = -9043565995104313970L;

            @Override
            public ExpirationPolicy buildTicketExpirationPolicy() {
                return NeverExpiresExpirationPolicy.INSTANCE;
            }
        };
    }

    @BeforeEach
    public void initialize(final TestInfo info, final RepetitionInfo repetitionInfo) {
        this.ticketGrantingTicketId = TICKET_GRANTING_TICKET_ID_GENERATOR.getNewTicketId(TicketGrantingTicket.PREFIX);
        this.serviceTicketId = new ServiceTicketIdGenerator(10, StringUtils.EMPTY)
            .getNewTicketId(ServiceTicket.PREFIX);
        this.proxyGrantingTicketId = new ProxyGrantingTicketIdGenerator(10, StringUtils.EMPTY)
            .getNewTicketId(ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX);
        this.transientSessionTicketId = new DefaultUniqueTicketIdGenerator().getNewTicketId(TransientSessionTicket.PREFIX);

        if (info.getTags().contains("TicketRegistryTestWithEncryption")) {
            useEncryption = true;
        } else if (info.getTags().contains("TicketRegistryTestWithoutEncryption")) {
            useEncryption = false;
        } else {
            useEncryption = repetitionInfo.getTotalRepetitions() == 2 && repetitionInfo.getCurrentRepetition() == 2;
        }
        ticketRegistry = this.getNewTicketRegistry();
        if (ticketRegistry != null) {
            ticketRegistry.deleteAll();
            setUpEncryption();
        }
    }

    @RepeatedTest(2)
    @Transactional(transactionManager = "ticketTransactionManager", readOnly = false)
    void verifyTicketsWithAuthnAttributes() throws Throwable {
        assumeTrue(isIterableRegistry());
        val authn = CoreAuthenticationTestUtils.getAuthentication(
            Map.of("cn", List.of("cn1", "cn2"), "givenName", List.of("g1", "g2"),
                "authn-context", List.of("mfa-example")));
        val tgt1 = new TicketGrantingTicketImpl(ticketGrantingTicketId,
            authn, NeverExpiresExpirationPolicy.INSTANCE);
        ticketRegistry.addTicket(tgt1);

        val tgt2 = new TicketGrantingTicketImpl(TICKET_GRANTING_TICKET_ID_GENERATOR.getNewTicketId(TicketGrantingTicket.PREFIX),
            CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        ticketRegistry.addTicket(tgt2);

        val queryAttributes = Map.<String, List<Object>>of("cn", List.of("cn2", "cn1000"),
            "authn-context", List.of("mfa-example", "mfa-one"));
        val tickets = ticketRegistry.getSessionsWithAttributes(queryAttributes).toList();
        assertEquals(1, tickets.size());
        assertTrue(tickets.contains(tgt1));
    }

    @RepeatedTest(2)
    void verifyAddTicketWithStream() throws Throwable {
        val originalAuthn = CoreAuthenticationTestUtils.getAuthentication();
        val s1 = Stream.of(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            originalAuthn, NeverExpiresExpirationPolicy.INSTANCE));
        ticketRegistry.addTicket(s1);
        val tgt = ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
        assertNotNull(tgt);
    }

    @RepeatedTest(2)
    void verifyUnableToAddExpiredTicket() throws Throwable {
        val originalAuthn = CoreAuthenticationTestUtils.getAuthentication();
        val s1 = Stream.of(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            originalAuthn, AlwaysExpiresExpirationPolicy.INSTANCE));
        ticketRegistry.addTicket(s1);
        assertThrows(InvalidTicketException.class, () -> ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class));
    }

    @RepeatedTest(2)
    void verifyAddTicketToCache() throws Throwable {
        val originalAuthn = CoreAuthenticationTestUtils.getAuthentication();
        ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            originalAuthn,
            NeverExpiresExpirationPolicy.INSTANCE));
        val tgt = ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
        assertNotNull(tgt);
        val authentication = tgt.getAuthentication();
        assertNotNull(authentication);
        assertNotNull(authentication.getSuccesses());
        assertNotNull(authentication.getWarnings());
        assertNotNull(authentication.getFailures());
    }

    @RepeatedTest(2)
    void verifyDeleteExpiredTicketById() throws Throwable {
        val expirationPolicy = new TicketGrantingTicketExpirationPolicy(42, 23);
        val ticketGrantingTicket = new TicketGrantingTicketImpl(ticketGrantingTicketId,
            CoreAuthenticationTestUtils.getAuthentication(), expirationPolicy);
        expirationPolicy.setClock(Clock.fixed(ticketGrantingTicket.getCreationTime().toInstant(), ZoneOffset.UTC));
        assertFalse(ticketGrantingTicket.isExpired());
        getNewTicketRegistry().addTicket(ticketGrantingTicket);
        ticketGrantingTicket.markTicketExpired();
        assertTrue(ticketGrantingTicket.isExpired());
        val deletedTicketCount = getNewTicketRegistry().deleteTicket(ticketGrantingTicket.getId());
        assertTrue(deletedTicketCount <= 1);
    }

    @RepeatedTest(2)
    void verifyTicketWithTimeoutPolicy() throws Throwable {
        val originalAuthn = CoreAuthenticationTestUtils.getAuthentication();
        ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            originalAuthn,
            TimeoutExpirationPolicy.builder().timeToKillInSeconds(5).build()));
        val tgt = ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
        assertNotNull(tgt);
    }

    @RepeatedTest(2)
    void verifyGetNullTicket() throws Throwable {
        assertThrows(InvalidTicketException.class, () -> ticketRegistry.getTicket(null, TicketGrantingTicket.class),
            () -> TICKET_SHOULD_BE_NULL_USE_ENCRYPTION + useEncryption + ']');
    }

    @RepeatedTest(2)
    void verifyGetNonExistingTicket() throws Throwable {
        assertThrows(InvalidTicketException.class, () -> ticketRegistry.getTicket("unknown-ticket", TicketGrantingTicket.class),
            () -> TICKET_SHOULD_BE_NULL_USE_ENCRYPTION + useEncryption + ']');
    }

    @RepeatedTest(2)
    void verifyGetExistingTicketWithProperClass() throws Throwable {
        ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            CoreAuthenticationTestUtils.getAuthentication(),
            NeverExpiresExpirationPolicy.INSTANCE));
        val ticket = ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
        assertNotNull(ticket, () -> "Ticket is null. useEncryption[" + useEncryption + ']');
        assertEquals(ticketGrantingTicketId, ticket.getId(), () -> "Ticket IDs don't match. useEncryption[" + useEncryption + ']');
    }

    @RepeatedTest(2)
    @Transactional(transactionManager = "ticketTransactionManager", readOnly = false)
    void verifyCountSessionsPerUser() throws Throwable {
        assumeTrue(isIterableRegistry());
        val id = UUID.randomUUID().toString();
        ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            CoreAuthenticationTestUtils.getAuthentication(id),
            NeverExpiresExpirationPolicy.INSTANCE));
        val count = ticketRegistry.countSessionsFor(id);
        assertTrue(count > 0);
    }

    @RepeatedTest(2)
    @Transactional
    void verifyGetSsoSessionsPerUser() throws Throwable {
        assumeTrue(isIterableRegistry());
        val id = UUID.randomUUID().toString();
        for (var i = 0; i < 5; i++) {
            val tgtId = TICKET_GRANTING_TICKET_ID_GENERATOR
                .getNewTicketId(TicketGrantingTicket.PREFIX);
            ticketRegistry.addTicket(new TicketGrantingTicketImpl(tgtId,
                CoreAuthenticationTestUtils.getAuthentication(id),
                NeverExpiresExpirationPolicy.INSTANCE));
        }
        try (val results = ticketRegistry.getSessionsFor(id)) {
            assertEquals(5, results.count());
        }
    }

    @RepeatedTest(2)
    void verifyGetExistingTicketWithImproperClass() throws Throwable {
        FunctionUtils.doAndRetry(callback -> {
            ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
                CoreAuthenticationTestUtils.getAuthentication(),
                NeverExpiresExpirationPolicy.INSTANCE));

            assertThrows(ClassCastException.class,
                () -> ticketRegistry.getTicket(ticketGrantingTicketId, ServiceTicket.class),
                () -> "Should throw ClassCastException. useEncryption[" + useEncryption + ']');
            return null;
        });
    }

    @RepeatedTest(2)
    void verifyGetNullTicketWithoutClass() throws Throwable {
        assertNull(ticketRegistry.getTicket(null), () -> TICKET_SHOULD_BE_NULL_USE_ENCRYPTION + useEncryption + ']');
    }

    @RepeatedTest(2)
    void verifyGetNonExistingTicketWithoutClass() throws Throwable {
        assertNull(ticketRegistry.getTicket("FALALALALALAL"), () -> TICKET_SHOULD_BE_NULL_USE_ENCRYPTION + useEncryption + ']');
    }

    @RepeatedTest(2)
    void verifyGetExistingTicket() throws Throwable {
        ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            CoreAuthenticationTestUtils.getAuthentication(),
            NeverExpiresExpirationPolicy.INSTANCE));
        val ticket = ticketRegistry.getTicket(ticketGrantingTicketId);
        assertNotNull(ticket, () -> "Ticket is null. useEncryption[" + useEncryption + ']');
        assertEquals(ticketGrantingTicketId, ticket.getId(), () -> "Ticket IDs don't match. useEncryption[" + useEncryption + ']');
    }

    /**
     * Exercise block of code in {@link AbstractTicketRegistry#getTicket(String ticketId)} that runs when
     * the method encounters a {@link Ticket} created in the future.
     * Adds 10 seconds to creation time to simulate time out of sync so warning will be logged.
     */
    @RepeatedTest(2)
    void verifyGetFutureDatedTicket() throws Throwable {
        val addTicket = new TicketGrantingTicketImpl(ticketGrantingTicketId,
            CoreAuthenticationTestUtils.getAuthentication(),
            NeverExpiresExpirationPolicy.INSTANCE);
        addTicket.setCreationTime(ZonedDateTime.now(addTicket.getExpirationPolicy().getClock()).plusSeconds(10));
        ticketRegistry.addTicket(addTicket);
        val ticket = ticketRegistry.getTicket(ticketGrantingTicketId);
        assertNotNull(ticket, () -> "Ticket is null. useEncryption[" + useEncryption + ']');
        assertEquals(ticketGrantingTicketId, ticket.getId(), () -> "Ticket IDs don't match. useEncryption[" + useEncryption + ']');
    }

    @RepeatedTest(2)
    void verifyAddAndUpdateTicket() throws Throwable {
        val tgt = new TicketGrantingTicketImpl(
            ticketGrantingTicketId,
            CoreAuthenticationTestUtils.getAuthentication(),
            NeverExpiresExpirationPolicy.INSTANCE);
        ticketRegistry.addTicket(tgt);

        await().untilAsserted(() -> assertNotNull(ticketRegistry.getTicket(tgt.getId(), TicketGrantingTicket.class)));

        val found = ticketRegistry.getTicket(tgt.getId(), TicketGrantingTicket.class);
        assertNotNull(found, () -> "Ticket is null. useEncryption[" + useEncryption + ']');

        assertInstanceOf(TicketGrantingTicket.class, found);
        var services = ((TicketGrantingTicket) found).getServices();
        assertTrue(services.isEmpty(), () -> "Ticket services should be empty. useEncryption[" + useEncryption + ']');

        val service = RegisteredServiceTestUtils.getService("TGT_UPDATE_TEST");
        tgt.grantServiceTicket("ST-1", service,
            NeverExpiresExpirationPolicy.INSTANCE, false, serviceTicketSessionTrackingPolicy);
        ticketRegistry.updateTicket(tgt);
        val tgtResult = ticketRegistry.getTicket(tgt.getId(), TicketGrantingTicket.class);
        assertInstanceOf(TicketGrantingTicket.class, tgtResult);
        services = ((TicketGrantingTicket) tgtResult).getServices();
        assertEquals(Collections.singleton("ST-1"), services.keySet());
    }

    @RepeatedTest(2)
    void verifyDeleteAllExistingTickets() throws Throwable {
        assumeTrue(isIterableRegistry());
        for (var i = 0; i < TICKETS_IN_REGISTRY; i++) {
            ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId + i,
                CoreAuthenticationTestUtils.getAuthentication(),
                NeverExpiresExpirationPolicy.INSTANCE));
        }
        val actual = ticketRegistry.deleteAll();
        if (actual <= 0) {
            LOGGER.warn("Ticket registry does not support reporting count of deleted rows");
        } else {
            assertEquals(TICKETS_IN_REGISTRY, actual, () -> "Wrong ticket count. useEncryption[" + useEncryption + ']');
        }
    }

    @RepeatedTest(2)
    void verifyDeleteExistingTicket() throws Throwable {
        ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            CoreAuthenticationTestUtils.getAuthentication(),
            NeverExpiresExpirationPolicy.INSTANCE));
        assertSame(1, ticketRegistry.deleteTicket(ticketGrantingTicketId), () -> "Wrong ticket count. useEncryption[" + useEncryption + ']');
        assertNull(ticketRegistry.getTicket(ticketGrantingTicketId), () -> TICKET_SHOULD_BE_NULL_USE_ENCRYPTION + useEncryption + ']');
    }

    @RepeatedTest(2)
    void verifyTransientSessionTickets() throws Throwable {
        ticketRegistry.addTicket(new TransientSessionTicketImpl(transientSessionTicketId, NeverExpiresExpirationPolicy.INSTANCE,
            RegisteredServiceTestUtils.getService(), CollectionUtils.wrap("key", "value")));
        assertSame(1, ticketRegistry.deleteTicket(transientSessionTicketId), () -> "Wrong ticket count. useEncryption[" + useEncryption + ']');
        assertNull(ticketRegistry.getTicket(transientSessionTicketId), () -> TICKET_SHOULD_BE_NULL_USE_ENCRYPTION + useEncryption + ']');
    }

    @RepeatedTest(2)
    @Transactional(transactionManager = "ticketTransactionManager", readOnly = false)
    void verifyDeleteNonExistingTicket() throws Throwable {
        ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            CoreAuthenticationTestUtils.getAuthentication(),
            NeverExpiresExpirationPolicy.INSTANCE));
        val ticketId = ticketGrantingTicketId + "NON-EXISTING-SUFFIX";
        ticketRegistry.deleteTicket(ticketId);
        assertEquals(0, ticketRegistry.getTickets(ticket -> ticket.getId().equals(ticketId)).count());
    }

    @RepeatedTest(2)
    void verifyDeleteNullTicket() throws Throwable {
        ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            CoreAuthenticationTestUtils.getAuthentication(),
            NeverExpiresExpirationPolicy.INSTANCE));
        assertNotEquals(1, ticketRegistry.deleteTicket(StringUtils.EMPTY), "Ticket was deleted.");
    }

    @RepeatedTest(2)
    void verifyGetTicketsIsZero() throws Throwable {
        ticketRegistry.deleteAll();
        assertEquals(0, ticketRegistry.getTickets().size(), "The size of the empty registry is not zero.");
    }

    @RepeatedTest(2)
    void verifyGetTicketsFromRegistryEqualToTicketsAdded() throws Throwable {
        assumeTrue(isIterableRegistry());
        val tickets = new ArrayList<Ticket>();

        for (var i = 0; i < TICKETS_IN_REGISTRY; i++) {
            val ticketGrantingTicket = new TicketGrantingTicketImpl(ticketGrantingTicketId + '-' + i,
                CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
            val st = ticketGrantingTicket.grantServiceTicket("ST-" + i,
                RegisteredServiceTestUtils.getService(),
                NeverExpiresExpirationPolicy.INSTANCE, false, serviceTicketSessionTrackingPolicy);
            tickets.add(ticketGrantingTicket);
            tickets.add(st);
            ticketRegistry.addTicket(ticketGrantingTicket);
            ticketRegistry.addTicket(st);
        }

        val ticketRegistryTickets = ticketRegistry.getTickets();
        assertEquals(tickets.size(), ticketRegistryTickets.size(), "The size of the registry is not the same as the collection.");


        tickets.stream().filter(ticket -> !ticketRegistryTickets.contains(ticket))
            .forEach(ticket -> {
                throw new AssertionError("Ticket " + ticket + " was not found in retrieval of collection of all tickets.");
            });
    }

    @RepeatedTest(1)
    @Tag("DisableTicketRegistryTestWithEncryption")
    void verifyTicketCountsEqualToTicketsAdded() throws Throwable {
        assumeTrue(isIterableRegistry());
        val tgts = new ArrayList<Ticket>();
        val sts = new ArrayList<Ticket>();

        FunctionUtils.doAndRetry(callback -> {
            for (var i = 0; i < TICKETS_IN_REGISTRY; i++) {
                val auth = CoreAuthenticationTestUtils.getAuthentication();
                val service = RegisteredServiceTestUtils.getService();
                val ticketGrantingTicket = new TicketGrantingTicketImpl(TicketGrantingTicket.PREFIX + '-' + i,
                    auth, NeverExpiresExpirationPolicy.INSTANCE);
                val st = ticketGrantingTicket.grantServiceTicket("ST-" + i,
                    service, NeverExpiresExpirationPolicy.INSTANCE, false, serviceTicketSessionTrackingPolicy);
                tgts.add(ticketGrantingTicket);
                sts.add(st);
                ticketRegistry.addTicket(ticketGrantingTicket);
                await().untilAsserted(() -> assertNotNull(ticketRegistry.getTicket(ticketGrantingTicket.getId()) != null));
                ticketRegistry.addTicket(st);
                await().untilAsserted(() -> assertNotNull(ticketRegistry.getTicket(st.getId()) != null));
            }
            await().untilAsserted(() -> {
                val sessionCount = ticketRegistry.sessionCount();
                assertEquals(tgts.size(), ticketRegistry.sessionCount(),
                    () -> "The sessionCount " + sessionCount + " is not the same as the collection " + tgts.size());
            });

            await().untilAsserted(() -> {
                val ticketCount = this.ticketRegistry.serviceTicketCount();
                assertEquals(sts.size(), ticketCount,
                    () -> "The serviceTicketCount " + ticketCount + " is not the same as the collection " + sts.size());
            });

            return null;
        });
    }

    @RepeatedTest(2)
    void verifyDeleteTicketWithChildren() throws Throwable {
        ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId + '1', CoreAuthenticationTestUtils.getAuthentication(),
            NeverExpiresExpirationPolicy.INSTANCE));
        val tgt = ticketRegistry.getTicket(ticketGrantingTicketId + '1', TicketGrantingTicket.class);

        val service = RegisteredServiceTestUtils.getService("TGT_DELETE_TEST");

        val st1 = tgt.grantServiceTicket("ST-11", service,
            NeverExpiresExpirationPolicy.INSTANCE, false, serviceTicketSessionTrackingPolicy);
        val st2 = tgt.grantServiceTicket("ST-21", service,
            NeverExpiresExpirationPolicy.INSTANCE, false, serviceTicketSessionTrackingPolicy);
        val st3 = tgt.grantServiceTicket("ST-31", service,
            NeverExpiresExpirationPolicy.INSTANCE, false, serviceTicketSessionTrackingPolicy);

        ticketRegistry.addTicket(st1);
        ticketRegistry.addTicket(st2);
        ticketRegistry.addTicket(st3);

        assertNotNull(ticketRegistry.getTicket(ticketGrantingTicketId + '1', TicketGrantingTicket.class));
        assertNotNull(ticketRegistry.getTicket("ST-11", ServiceTicket.class));
        assertNotNull(ticketRegistry.getTicket("ST-21", ServiceTicket.class));
        assertNotNull(ticketRegistry.getTicket("ST-31", ServiceTicket.class));

        ticketRegistry.updateTicket(tgt);
        assertSame(4, ticketRegistry.deleteTicket(tgt.getId()));

        assertThrows(InvalidTicketException.class, () -> ticketRegistry.getTicket(ticketGrantingTicketId + '1', TicketGrantingTicket.class));
        assertThrows(InvalidTicketException.class, () -> ticketRegistry.getTicket("ST-11", ServiceTicket.class));
        assertThrows(InvalidTicketException.class, () -> ticketRegistry.getTicket("ST-21", ServiceTicket.class));
        assertThrows(InvalidTicketException.class, () -> ticketRegistry.getTicket("ST-31", ServiceTicket.class));
    }

    @RepeatedTest(2)
    void verifyWriteGetDelete() throws Throwable {
        val ticket = new TicketGrantingTicketImpl(ticketGrantingTicketId,
            CoreAuthenticationTestUtils.getAuthentication(),
            NeverExpiresExpirationPolicy.INSTANCE);
        ticketRegistry.addTicket(ticket);
        val ticketFromRegistry = ticketRegistry.getTicket(ticketGrantingTicketId);
        assertNotNull(ticketFromRegistry);
        assertEquals(ticketGrantingTicketId, ticketFromRegistry.getId());
        ticketRegistry.deleteTicket(ticketGrantingTicketId);
        assertNull(ticketRegistry.getTicket(ticketGrantingTicketId));
    }

    @RepeatedTest(2)
    void verifyExpiration() throws Throwable {
        val authn = CoreAuthenticationTestUtils.getAuthentication();
        LOGGER.trace("Adding ticket [{}]", ticketGrantingTicketId);
        ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId, authn, NeverExpiresExpirationPolicy.INSTANCE));
        LOGGER.trace("Getting ticket [{}]", ticketGrantingTicketId);
        val tgt = ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
        assertNotNull(tgt, () -> "Ticket-granting ticket " + ticketGrantingTicketId + " cannot be fetched");
        val service = RegisteredServiceTestUtils.getService("TGT_DELETE_TEST");
        LOGGER.trace("Granting service ticket [{}]", serviceTicketId);
        val ticket = (AbstractTicket) tgt.grantServiceTicket(serviceTicketId, service,
            NeverExpiresExpirationPolicy.INSTANCE, false, serviceTicketSessionTrackingPolicy);
        assertNotNull(ticket, "Service ticket cannot be null");
        ticket.setExpirationPolicy(AlwaysExpiresExpirationPolicy.INSTANCE);
        ticketRegistry.addTicket(ticket);
        ticketRegistry.updateTicket(tgt);
        assertThrows(InvalidTicketException.class, () -> ticketRegistry.getTicket(serviceTicketId, ServiceTicket.class));
    }

    @RepeatedTest(2)
    void verifyExpiredTicket() throws Throwable {
        val authn = CoreAuthenticationTestUtils.getAuthentication();
        ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId, authn, AlwaysExpiresExpirationPolicy.INSTANCE));
        assertThrows(InvalidTicketException.class, () -> ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class));
    }

    @RepeatedTest(2)
    void verifyDeleteTicketWithPGT() throws Throwable {
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            authentication, NeverExpiresExpirationPolicy.INSTANCE));
        val tgt = ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);

        val service = RegisteredServiceTestUtils.getService("TGT_DELETE_TEST");

        val st1 = (ProxyGrantingTicketIssuerTicket) tgt.grantServiceTicket(serviceTicketId,
            service, NeverExpiresExpirationPolicy.INSTANCE, false, serviceTicketSessionTrackingPolicy);
        ticketRegistry.addTicket(st1);
        ticketRegistry.updateTicket(tgt);

        assertNotNull(ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class));
        assertNotNull(ticketRegistry.getTicket(serviceTicketId, ServiceTicket.class));

        val pgt = st1.grantProxyGrantingTicket(proxyGrantingTicketId, authentication, NeverExpiresExpirationPolicy.INSTANCE);
        ticketRegistry.addTicket(pgt);
        ticketRegistry.updateTicket(tgt);
        ticketRegistry.updateTicket(st1);
        assertEquals(pgt.getTicketGrantingTicket(), tgt);
        assertNotNull(ticketRegistry.getTicket(proxyGrantingTicketId, ProxyGrantingTicket.class));
        assertEquals(authentication, pgt.getAuthentication());
        assertNotNull(ticketRegistry.getTicket(serviceTicketId, ServiceTicket.class));

        await().untilAsserted(() -> assertTrue(ticketRegistry.deleteTicket(tgt.getId()) > 0));

        assertThrows(InvalidTicketException.class, () -> ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class));
        assertThrows(InvalidTicketException.class, () -> ticketRegistry.getTicket(serviceTicketId, ServiceTicket.class));
        assertThrows(InvalidTicketException.class, () -> ticketRegistry.getTicket(proxyGrantingTicketId, ProxyGrantingTicket.class));
    }

    @RepeatedTest(2)
    void verifyDeleteTicketsWithMultiplePGTs() throws Throwable {
        FunctionUtils.doAndRetry(callback -> {
            val a = CoreAuthenticationTestUtils.getAuthentication();
            ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId, a, NeverExpiresExpirationPolicy.INSTANCE));
            val tgt = ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
            assertNotNull(tgt, "Ticket-granting ticket must not be null");
            val service = RegisteredServiceTestUtils.getService("TGT_DELETE_TEST");
            IntStream.range(1, 5).forEach(Unchecked.intConsumer(i -> {
                val st = (ProxyGrantingTicketIssuerTicket) tgt.grantServiceTicket(serviceTicketId + '-' + i, service,
                    NeverExpiresExpirationPolicy.INSTANCE, false, serviceTicketSessionTrackingPolicy);
                ticketRegistry.addTicket(st);
                ticketRegistry.updateTicket(tgt);

                val pgt = st.grantProxyGrantingTicket(proxyGrantingTicketId + '-' + i, a, NeverExpiresExpirationPolicy.INSTANCE);
                ticketRegistry.addTicket(pgt);
                ticketRegistry.updateTicket(tgt);
                ticketRegistry.updateTicket(st);
            }));

            val count = ticketRegistry.deleteTicket(ticketGrantingTicketId);
            assertEquals(9, count);
            return null;
        });
    }

    protected abstract TicketRegistry getNewTicketRegistry();

    /**
     * Determine whether the tested registry is able to iterate its tickets.
     */
    protected boolean isIterableRegistry() {
        return true;
    }

    protected CipherExecutor setupCipherExecutor() {
        return CoreTicketUtils.newTicketRegistryCipherExecutor(
            new EncryptionRandomizedSigningJwtCryptographyProperties(), "[tests]");
    }

    private void setUpEncryption() {
        var registry = (AbstractTicketRegistry) AopTestUtils.getTargetObject(ticketRegistry);
        if (this.useEncryption) {
            registry.setCipherExecutor(setupCipherExecutor());
        } else {
            registry.setCipherExecutor(CipherExecutor.noOp());
        }
    }

    @ImportAutoConfiguration({
        ObservationAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        RefreshAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        CasCoreHttpConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCoreTicketIdGeneratorsConfiguration.class,
        CasCoreTicketsSchedulingConfiguration.class,
        CasCoreTicketsSerializationConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        CasPersonDirectoryStubConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreServicesAuthenticationConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationPolicyConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationHandlersConfiguration.class,
        CasCoreConfiguration.class,
        CasCookieConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCoreNotificationsConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
