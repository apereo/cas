package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
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
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.ServiceTicketIdGenerator;
import org.apereo.cas.util.TicketGrantingTicketIdGenerator;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.AopTestUtils;
import org.springframework.transaction.annotation.Transactional;
import java.io.Serial;
import java.time.Clock;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
@ExtendWith(CasTestExtension.class)
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

    @Autowired
    @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
    protected ServiceFactory<WebApplicationService> webApplicationServiceFactory;
    
    protected boolean useEncryption;
    
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
    void initialize(final TestInfo info, final RepetitionInfo repetitionInfo) {
        if (info.getTags().contains("TicketRegistryTestWithEncryption")) {
            useEncryption = true;
        } else if (info.getTags().contains("TicketRegistryTestWithoutEncryption")) {
            useEncryption = false;
        } else {
            useEncryption = repetitionInfo.getTotalRepetitions() == 2 && repetitionInfo.getCurrentRepetition() == 2;
        }
        ticketRegistry = this.getNewTicketRegistry();
        if (ticketRegistry != null) {
            if (!info.getTags().contains("SkipClearingTicketRegistry")) {
                ticketRegistry.deleteAll();
            }
            setUpEncryption();
        }
    }

    @RepeatedTest(2)
    @Transactional(transactionManager = "ticketTransactionManager", readOnly = false)
    void verifyTicketsWithAuthnAttributes() throws Throwable {
        assumeTrue(canTicketRegistryIterate());
        val authn = CoreAuthenticationTestUtils.getAuthentication(
            Map.of("cn", List.of("cn1", "cn2"), "givenName", List.of("g1", "g2"),
                "authn-context", List.of("mfa-example")));
        val tgt1 = new TicketGrantingTicketImpl(TestTicketIdentifiers.generate().ticketGrantingTicketId(),
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
    void verifyAddTicketWithStream() {
        val originalAuthn = CoreAuthenticationTestUtils.getAuthentication();
        val ticketGrantingTicket = new TicketGrantingTicketImpl(TestTicketIdentifiers.generate().ticketGrantingTicketId(),
            originalAuthn, NeverExpiresExpirationPolicy.INSTANCE);
        val streamToAdd = Stream.of(ticketGrantingTicket);
        val addedTickets = ticketRegistry.addTicket(streamToAdd);
        val ticketToFetch = addedTickets.isEmpty() ? ticketGrantingTicket.getId() : addedTickets.getFirst().getId();
        val tgt = ticketRegistry.getTicket(ticketToFetch, TicketGrantingTicket.class);
        assertNotNull(tgt);
    }

    @RepeatedTest(2)
    void verifyUnableToAddExpiredTicket() {
        val originalAuthn = CoreAuthenticationTestUtils.getAuthentication();
        val ticketGrantingTicketId = TestTicketIdentifiers.generate().ticketGrantingTicketId();
        val s1 = Stream.of(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            originalAuthn, AlwaysExpiresExpirationPolicy.INSTANCE));
        ticketRegistry.addTicket(s1);
        assertThrows(InvalidTicketException.class, () -> ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class));
    }

    @RepeatedTest(2)
    void verifyAddTicketToCache() throws Throwable {
        val originalAuthn = CoreAuthenticationTestUtils.getAuthentication();
        val ticketGrantingTicketId = TestTicketIdentifiers.generate().ticketGrantingTicketId();
        val addedTicket = ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            originalAuthn, NeverExpiresExpirationPolicy.INSTANCE));
        val tgt = ticketRegistry.getTicket(addedTicket.getId(), TicketGrantingTicket.class);
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
        val ticketGrantingTicketId = TestTicketIdentifiers.generate().ticketGrantingTicketId();
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
        val ticketGrantingTicketId = TestTicketIdentifiers.generate().ticketGrantingTicketId();
        val addedTicket = ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            originalAuthn, TimeoutExpirationPolicy.builder().timeToKillInSeconds(5).build()));
        val tgt = ticketRegistry.getTicket(addedTicket.getId(), TicketGrantingTicket.class);
        assertNotNull(tgt);
    }

    @RepeatedTest(2)
    void verifyGetNullTicket() {
        assertThrows(InvalidTicketException.class, () -> ticketRegistry.getTicket(null, TicketGrantingTicket.class),
            () -> TICKET_SHOULD_BE_NULL_USE_ENCRYPTION + useEncryption + ']');
    }

    @RepeatedTest(2)
    void verifyGetNonExistingTicket() {
        assertThrows(InvalidTicketException.class, () -> ticketRegistry.getTicket("unknown-ticket", TicketGrantingTicket.class),
            () -> TICKET_SHOULD_BE_NULL_USE_ENCRYPTION + useEncryption + ']');
    }

    @RepeatedTest(2)
    void verifyGetExistingTicketWithProperClass() throws Throwable {
        val ticketGrantingTicketId = TestTicketIdentifiers.generate().ticketGrantingTicketId();
        val addedTicket = ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            CoreAuthenticationTestUtils.getAuthentication(),
            NeverExpiresExpirationPolicy.INSTANCE));
        val ticket = ticketRegistry.getTicket(addedTicket.getId(), TicketGrantingTicket.class);
        assertNotNull(ticket, () -> "Ticket is null. useEncryption[" + useEncryption + ']');
        assertEquals(ticketGrantingTicketId, ticket.getId(), () -> "Ticket IDs don't match. useEncryption[" + useEncryption + ']');
    }

    @RepeatedTest(2)
    @Transactional(transactionManager = "ticketTransactionManager", readOnly = false)
    void verifyCountSessionsPerUser() throws Throwable {
        val ticketGrantingTicketId = TestTicketIdentifiers.generate().ticketGrantingTicketId();
        assumeTrue(canTicketRegistryIterate());
        val id = UUID.randomUUID().toString();
        ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            CoreAuthenticationTestUtils.getAuthentication(id),
            NeverExpiresExpirationPolicy.INSTANCE));
        val count = ticketRegistry.countSessionsFor(id);
        assertTrue(count > 0);
    }

    @RepeatedTest(2)
    @Transactional(transactionManager = "ticketTransactionManager", readOnly = false)
    void verifyGetSsoSessionsPerUser() throws Throwable {
        val assumption = "Ticket registry %s does not support iteration".formatted(getClass().getName());
        assumeTrue(canTicketRegistryIterate(), assumption);
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
        val ticketGrantingTicketId = TestTicketIdentifiers.generate().ticketGrantingTicketId();
        FunctionUtils.doAndRetry(callback -> {
            val added = ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
                CoreAuthenticationTestUtils.getAuthentication(),
                NeverExpiresExpirationPolicy.INSTANCE));

            assertThrows(ClassCastException.class,
                () -> ticketRegistry.getTicket(added.getId(), ServiceTicket.class),
                () -> "Should throw ClassCastException. useEncryption[" + useEncryption + ']');
            return null;
        });
    }

    @RepeatedTest(2)
    void verifyGetNullTicketWithoutClass() {
        assertNull(ticketRegistry.getTicket(null), () -> TICKET_SHOULD_BE_NULL_USE_ENCRYPTION + useEncryption + ']');
    }

    @RepeatedTest(2)
    void verifyGetNonExistingTicketWithoutClass() {
        assertNull(ticketRegistry.getTicket("FALALALALALAL"), () -> TICKET_SHOULD_BE_NULL_USE_ENCRYPTION + useEncryption + ']');
    }

    @RepeatedTest(2)
    void verifyGetExistingTicket() throws Throwable {
        val ticketGrantingTicketId = TestTicketIdentifiers.generate().ticketGrantingTicketId();
        val added = ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            CoreAuthenticationTestUtils.getAuthentication(),
            NeverExpiresExpirationPolicy.INSTANCE));
        val ticket = ticketRegistry.getTicket(added.getId());
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
        val ticketGrantingTicketId = TestTicketIdentifiers.generate().ticketGrantingTicketId();
        val addTicket = new TicketGrantingTicketImpl(ticketGrantingTicketId,
            CoreAuthenticationTestUtils.getAuthentication(),
            NeverExpiresExpirationPolicy.INSTANCE);
        addTicket.setCreationTime(ZonedDateTime.now(addTicket.getExpirationPolicy().getClock()).plusSeconds(10));
        val addedTicket = ticketRegistry.addTicket(addTicket);
        val ticket = ticketRegistry.getTicket(addedTicket.getId());
        assertNotNull(ticket, () -> "Ticket is null. useEncryption[" + useEncryption + ']');
        assertEquals(ticketGrantingTicketId, ticket.getId(), () -> "Ticket IDs don't match. useEncryption[" + useEncryption + ']');
    }

    @RepeatedTest(2)
    void verifyAddAndUpdateTicket() throws Throwable {
        val generatedTickets = TestTicketIdentifiers.generate();
        val ticketGrantingTicketId = generatedTickets.ticketGrantingTicketId();
        val tgt = new TicketGrantingTicketImpl(
            ticketGrantingTicketId,
            CoreAuthenticationTestUtils.getAuthentication(),
            NeverExpiresExpirationPolicy.INSTANCE);
        val addedTicket = ticketRegistry.addTicket(tgt);

        await().untilAsserted(() -> assertNotNull(ticketRegistry.getTicket(addedTicket.getId(), TicketGrantingTicket.class)));

        val found = ticketRegistry.getTicket(addedTicket.getId(), TicketGrantingTicket.class);
        assertNotNull(found, () -> "Ticket is null. useEncryption[" + useEncryption + ']');

        assertInstanceOf(TicketGrantingTicket.class, found);
        var services = found.getServices();
        assertTrue(services.isEmpty(), () -> "Ticket services should be empty. useEncryption[" + useEncryption + ']');

        val service = RegisteredServiceTestUtils.getService("TGT_UPDATE_TEST");
        val serviceTicketId = generatedTickets.serviceTicketId();
        val serviceTicket = tgt.grantServiceTicket(serviceTicketId, service,
            NeverExpiresExpirationPolicy.INSTANCE, false, serviceTicketSessionTrackingPolicy);
        assertNotNull(serviceTicket);

        val updatedTgt = ticketRegistry.updateTicket(tgt);
        val tgtResult = updatedTgt.isStateless()
            ? ticketRegistry.getTicket(updatedTgt.getId(), TicketGrantingTicket.class)
            : ticketRegistry.getTicket(tgt.getId(), TicketGrantingTicket.class);
        assertInstanceOf(TicketGrantingTicket.class, tgtResult);
        services = tgtResult.getServices();
        assertEquals(Set.of(serviceTicketId), services.keySet());
    }

    @RepeatedTest(2)
    void verifyCountingTicketsForService() throws Throwable {
        assumeTrue(canTicketRegistryIterate());
        val authentication = CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString());
        for (var i = 0; i < 10; i++) {
            val tgt = new TicketGrantingTicketImpl(
                TICKET_GRANTING_TICKET_ID_GENERATOR.getNewTicketId(TicketGrantingTicket.PREFIX),
                authentication,
                NeverExpiresExpirationPolicy.INSTANCE);
            val addedTicket = ticketRegistry.addTicket(tgt);
            val foundTgt = ticketRegistry.getTicket(addedTicket.getId(), TicketGrantingTicket.class);
            assertNotNull(foundTgt);

            val service = i % 2 == 0 ? RegisteredServiceTestUtils.getService() : RegisteredServiceTestUtils.getService2();
            val serviceTicket = foundTgt.grantServiceTicket("ST-%s".formatted(RandomUtils.generateSecureRandomId()),
                service, NeverExpiresExpirationPolicy.INSTANCE, false, serviceTicketSessionTrackingPolicy);
            ticketRegistry.updateTicket(foundTgt);
            ticketRegistry.addTicket(serviceTicket);
        }
        assertEquals(5, ticketRegistry.countTicketsFor(RegisteredServiceTestUtils.getService()));
    }

    @RepeatedTest(2)
    void verifyDeleteAllExistingTickets() throws Throwable {
        assumeTrue(canTicketRegistryIterate());
        val ticketGrantingTicketId = TestTicketIdentifiers.generate().ticketGrantingTicketId();
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
        assumeTrue(canTicketRegistryDelete());
        val ticketGrantingTicketId = TestTicketIdentifiers.generate().ticketGrantingTicketId();
        val addedTicket = ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            CoreAuthenticationTestUtils.getAuthentication(),
            NeverExpiresExpirationPolicy.INSTANCE));
        assertSame(1, ticketRegistry.deleteTicket(addedTicket.getId()), () -> "Wrong ticket count. useEncryption[" + useEncryption + ']');
        assertNull(ticketRegistry.getTicket(addedTicket.getId()), () -> TICKET_SHOULD_BE_NULL_USE_ENCRYPTION + useEncryption + ']');
    }

    @RepeatedTest(2)
    void verifyTransientSessionTickets() throws Throwable {
        assumeTrue(canTicketRegistryDelete());
        val transientSessionTicketId = TestTicketIdentifiers.generate().transientSessionTicketId();
        val addedTicket = ticketRegistry.addTicket(new TransientSessionTicketImpl(
            transientSessionTicketId, NeverExpiresExpirationPolicy.INSTANCE,
            RegisteredServiceTestUtils.getService(), CollectionUtils.wrap("key", "value")));
        assertSame(1, ticketRegistry.deleteTicket(addedTicket.getId()), () -> "Wrong ticket count. useEncryption[" + useEncryption + ']');
        assertNull(ticketRegistry.getTicket(addedTicket.getId()), () -> TICKET_SHOULD_BE_NULL_USE_ENCRYPTION + useEncryption + ']');
    }

    @RepeatedTest(2)
    @Transactional(transactionManager = "ticketTransactionManager", readOnly = false)
    void verifyDeleteNonExistingTicket() throws Throwable {
        val ticketGrantingTicketId = TestTicketIdentifiers.generate().ticketGrantingTicketId();
        val addedTicket = ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            CoreAuthenticationTestUtils.getAuthentication(),
            NeverExpiresExpirationPolicy.INSTANCE));
        val ticketId = addedTicket.getId() + "NON-EXISTING-SUFFIX";
        ticketRegistry.deleteTicket(ticketId);
        assertEquals(0, ticketRegistry.getTickets(ticket -> ticket.getId().equals(ticketId)).count());
    }

    @RepeatedTest(2)
    void verifyDeleteNullTicket() throws Throwable {
        val ticketGrantingTicketId = TestTicketIdentifiers.generate().ticketGrantingTicketId();
        ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            CoreAuthenticationTestUtils.getAuthentication(),
            NeverExpiresExpirationPolicy.INSTANCE));
        assertNotEquals(1, ticketRegistry.deleteTicket(StringUtils.EMPTY), "Ticket was deleted.");
    }

    @RepeatedTest(2)
    void verifyGetTicketsIsZero() {
        ticketRegistry.deleteAll();
        assertEquals(0, ticketRegistry.getTickets().size(), "The size of the empty registry is not zero.");
    }

    @RepeatedTest(2)
    void verifyGetTicketsFromRegistryEqualToTicketsAdded() throws Throwable {
        assumeTrue(canTicketRegistryIterate());
        val tickets = new ArrayList<Ticket>();

        val ticketGrantingTicketId = TestTicketIdentifiers.generate().ticketGrantingTicketId();
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
        assumeTrue(canTicketRegistryIterate());
        val ticketGrantingTickets = new ArrayList<Ticket>();
        val serviceTickets = new ArrayList<Ticket>();
        FunctionUtils.doAndRetry(callback -> {
            ticketGrantingTickets.clear();
            serviceTickets.clear();
            for (var i = 0; i < TICKETS_IN_REGISTRY; i++) {
                val auth = CoreAuthenticationTestUtils.getAuthentication();
                val service = RegisteredServiceTestUtils.getService();
                val ticketGrantingTicket = new TicketGrantingTicketImpl(TicketGrantingTicket.PREFIX + '-' + i,
                    auth, NeverExpiresExpirationPolicy.INSTANCE);
                val st = ticketGrantingTicket.grantServiceTicket("ST-" + i,
                    service, NeverExpiresExpirationPolicy.INSTANCE, false, serviceTicketSessionTrackingPolicy);
                ticketGrantingTickets.add(ticketGrantingTicket);
                serviceTickets.add(st);
                val addedTicket = ticketRegistry.addTicket(ticketGrantingTicket);
                await().untilAsserted(() -> assertNotNull(ticketRegistry.getTicket(addedTicket.getId())));
                val addedServiceTicket = ticketRegistry.addTicket(st);
                await().untilAsserted(() -> assertNotNull(ticketRegistry.getTicket(addedServiceTicket.getId())));
            }
            await().untilAsserted(() -> {
                val sessionCount = ticketRegistry.sessionCount();
                assertEquals(ticketGrantingTickets.size(), sessionCount,
                    () -> "The sessionCount " + sessionCount + " is not the same as the collection " + ticketGrantingTickets.size());
            });

            await().untilAsserted(() -> {
                val ticketCount = ticketRegistry.serviceTicketCount();
                assertEquals(serviceTickets.size(), ticketCount,
                    () -> "The serviceTicketCount " + ticketCount + " is not the same as the collection " + serviceTickets.size());
            });
            return null;
        });
    }

    @RepeatedTest(2)
    void verifyDeleteTicketWithChildren() throws Throwable {
        assumeTrue(canTicketRegistryDelete());
        val ticketGrantingTicketId = TestTicketIdentifiers.generate().ticketGrantingTicketId();
        val addedTicket = ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId + '1',
            CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE));
        val tgt = ticketRegistry.getTicket(addedTicket.getId(), TicketGrantingTicket.class);

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
        val ticketGrantingTicketId = TestTicketIdentifiers.generate().ticketGrantingTicketId();
        val ticket = new TicketGrantingTicketImpl(ticketGrantingTicketId,
            CoreAuthenticationTestUtils.getAuthentication(),
            NeverExpiresExpirationPolicy.INSTANCE);
        val addedTicket = ticketRegistry.addTicket(ticket);
        val ticketFromRegistry = ticketRegistry.getTicket(addedTicket.getId());
        assertNotNull(ticketFromRegistry);
        assumeTrue(canTicketRegistryDelete());
        ticketRegistry.deleteTicket(addedTicket.getId());
        assertNull(ticketRegistry.getTicket(addedTicket.getId()));
    }

    @RepeatedTest(2)
    void verifyExpiration() throws Throwable {
        val ticketGrantingTicketId = TestTicketIdentifiers.generate().ticketGrantingTicketId();
        val serviceTicketId = TestTicketIdentifiers.generate().serviceTicketId();
        val authn = CoreAuthenticationTestUtils.getAuthentication();
        LOGGER.trace("Adding ticket [{}]", ticketGrantingTicketId);
        var addedTicket = ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId, authn, NeverExpiresExpirationPolicy.INSTANCE));
        LOGGER.trace("Getting ticket [{}]", addedTicket.getId());
        val tgt = ticketRegistry.getTicket(addedTicket.getId(), TicketGrantingTicket.class);
        assertNotNull(tgt, () -> "Ticket-granting ticket " + addedTicket.getId() + " cannot be fetched");
        val service = RegisteredServiceTestUtils.getService("TGT_DELETE_TEST");
        LOGGER.trace("Granting service ticket [{}]", serviceTicketId);
        val serviceTicket = (AbstractTicket) tgt.grantServiceTicket(serviceTicketId, service,
            NeverExpiresExpirationPolicy.INSTANCE, false, serviceTicketSessionTrackingPolicy);
        assertNotNull(serviceTicket, "Service ticket cannot be null");
        serviceTicket.setExpirationPolicy(AlwaysExpiresExpirationPolicy.INSTANCE);
        ticketRegistry.addTicket(serviceTicket);
        ticketRegistry.updateTicket(tgt);
        assertThrows(InvalidTicketException.class, () -> ticketRegistry.getTicket(serviceTicketId, ServiceTicket.class));
    }

    @RepeatedTest(2)
    void verifyExpiredTicket() throws Throwable {
        val ticketGrantingTicketId = TestTicketIdentifiers.generate().ticketGrantingTicketId();
        val authn = CoreAuthenticationTestUtils.getAuthentication();
        ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId, authn, AlwaysExpiresExpirationPolicy.INSTANCE));
        assertThrows(InvalidTicketException.class, () -> ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class));
    }

    @RepeatedTest(2)
    void verifyDeleteTicketWithPGT() throws Throwable {
        assumeTrue(canTicketRegistryDelete());
        val ticketGrantingTicketId = TestTicketIdentifiers.generate().ticketGrantingTicketId();
        val serviceTicketId = TestTicketIdentifiers.generate().serviceTicketId();

        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        val addedTicket = ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            authentication, NeverExpiresExpirationPolicy.INSTANCE));
        val tgt = ticketRegistry.getTicket(addedTicket.getId(), TicketGrantingTicket.class);

        val service = RegisteredServiceTestUtils.getService("TGT_DELETE_TEST");

        val st1 = (ProxyGrantingTicketIssuerTicket) tgt.grantServiceTicket(serviceTicketId,
            service, NeverExpiresExpirationPolicy.INSTANCE, false, serviceTicketSessionTrackingPolicy);
        val addedServiceTicket = ticketRegistry.addTicket(st1);
        ticketRegistry.updateTicket(tgt);

        assertNotNull(ticketRegistry.getTicket(addedTicket.getId(), TicketGrantingTicket.class));
        assertNotNull(ticketRegistry.getTicket(addedServiceTicket.getId(), ServiceTicket.class));

        val proxyGrantingTicketId = TestTicketIdentifiers.generate().proxyGrantingTicketId();
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
        assumeTrue(canTicketRegistryDelete());
        FunctionUtils.doAndRetry(callback -> {
            val authentication = CoreAuthenticationTestUtils.getAuthentication();
            val ticketGrantingTicketId = TestTicketIdentifiers.generate().ticketGrantingTicketId();
            val serviceTicketId = TestTicketIdentifiers.generate().serviceTicketId();

            ticketRegistry.addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId, authentication, NeverExpiresExpirationPolicy.INSTANCE));
            val tgt = ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
            assertNotNull(tgt, "Ticket-granting ticket must not be null");
            val service = RegisteredServiceTestUtils.getService("TGT_DELETE_TEST");
            IntStream.range(1, 5).forEach(Unchecked.intConsumer(i -> {
                val st = (ProxyGrantingTicketIssuerTicket) tgt.grantServiceTicket(serviceTicketId + '-' + i, service,
                    NeverExpiresExpirationPolicy.INSTANCE, false, serviceTicketSessionTrackingPolicy);
                ticketRegistry.addTicket(st);
                ticketRegistry.updateTicket(tgt);

                val proxyGrantingTicketId = TestTicketIdentifiers.generate().proxyGrantingTicketId();
                val pgt = st.grantProxyGrantingTicket(proxyGrantingTicketId + '-' + i, authentication, NeverExpiresExpirationPolicy.INSTANCE);
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
    protected boolean canTicketRegistryIterate() {
        return true;
    }

    protected boolean canTicketRegistryDelete() {
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
        CasCoreTicketsAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreScriptingAutoConfiguration.class,
        CasPersonDirectoryAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreCookieAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreWebflowAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class
    })
    @SpringBootConfiguration(proxyBeanMethods = false)
    @SpringBootTestAutoConfigurations
    public static class SharedTestConfiguration {
    }

    record TestTicketIdentifiers(
        String ticketGrantingTicketId, String serviceTicketId,
        String transientSessionTicketId, String proxyGrantingTicketId) {

        static TestTicketIdentifiers generate() {
            var ticketGrantingTicketId = TICKET_GRANTING_TICKET_ID_GENERATOR.getNewTicketId(TicketGrantingTicket.PREFIX);
            var serviceTicketId = new ServiceTicketIdGenerator(10, StringUtils.EMPTY)
                .getNewTicketId(ServiceTicket.PREFIX);
            var proxyGrantingTicketId = new ProxyGrantingTicketIdGenerator(10, StringUtils.EMPTY)
                .getNewTicketId(ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX);
            var transientSessionTicketId = new DefaultUniqueTicketIdGenerator().getNewTicketId(TransientSessionTicket.PREFIX);
            return new TestTicketIdentifiers(ticketGrantingTicketId, serviceTicketId, transientSessionTicketId, proxyGrantingTicketId);
        }
    }
}
