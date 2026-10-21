package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.config.CasHazelcastTicketRegistryAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.DefaultTicketCatalog;
import org.apereo.cas.ticket.DefaultTicketDefinition;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketImpl;
import org.apereo.cas.ticket.expiration.BaseDelegatingExpirationPolicy;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.expiration.RememberMeDelegatingExpirationPolicy;
import org.apereo.cas.util.TicketGrantingTicketIdGenerator;
import org.apereo.cas.util.crypto.CipherExecutor;
import com.hazelcast.config.Config;
import com.hazelcast.core.EntryView;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link HazelcastTicketRegistry}.
 *
 * @author Dmitriy Kopylenko
 * @since 4.1.0
 */
@Tag("Hazelcast")
@Slf4j
@ExtendWith(CasTestExtension.class)
class HazelcastTicketRegistryTests {
    @Nested
    @Getter
    @ImportAutoConfiguration(CasHazelcastTicketRegistryAutoConfiguration.class)
    @Import(BaseTicketRegistryTests.SharedTestConfiguration.class)
    @TestPropertySource(
        properties = {
            "cas.ticket.registry.hazelcast.core.enable-jet=false",
            "cas.ticket.registry.hazelcast.cluster.network.port=5707",
            "cas.ticket.registry.hazelcast.cluster.network.tcpip-enabled=false",
            "cas.ticket.registry.hazelcast.cluster.core.instance-name=testjetlessinstance",
            "cas.ticket.registry.hazelcast.crypto.enabled=false"
        })
    @Tag("TicketRegistryTestWithoutEncryption")
    class JetlessTests extends BaseHazelcastTicketRegistryTests {
        @Autowired
        @Qualifier(TicketRegistry.BEAN_NAME)
        private TicketRegistry newTicketRegistry;
    }

    @Nested
    @Getter
    @ImportAutoConfiguration(CasHazelcastTicketRegistryAutoConfiguration.class)
    @Import(BaseTicketRegistryTests.SharedTestConfiguration.class)
    @TestPropertySource(
        properties = {
            "cas.ticket.registry.hazelcast.cluster.network.port=5703",
            "cas.ticket.registry.hazelcast.cluster.network.tcpip-enabled=false",
            "cas.ticket.registry.hazelcast.cluster.core.instance-name=testlocalhostinstance",
            "cas.ticket.registry.hazelcast.crypto.enabled=true"
        })
    @Tag("TicketRegistryTestWithEncryption")
    class DefaultTests extends BaseHazelcastTicketRegistryTests {
        @Autowired
        @Qualifier(TicketRegistry.BEAN_NAME)
        private TicketRegistry newTicketRegistry;

        @Autowired
        private CasConfigurationProperties casProperties;

        @Autowired
        @Qualifier("casTicketRegistryHazelcastInstance")
        private HazelcastInstance hazelcastInstance;

        @RepeatedTest(1)
        void verifyBadExpPolicyValue() {
            val ticket = new MockTicketGrantingTicket("casuser");

            val instance = mock(HazelcastInstance.class);
            val myMap = mock(IMap.class);
            when(instance.getMap(anyString())).thenReturn(myMap);
            when(instance.getConfig()).thenReturn(new Config());
            val registry = new HazelcastTicketRegistry(CipherExecutor.noOp(), ticketSerializationManager, ticketCatalog,
                applicationContext, instance, casProperties.getTicket().getRegistry().getHazelcast());
            ticket.setExpirationPolicy(new HardTimeoutExpirationPolicy(-1));
            assertDoesNotThrow(() -> registry.addTicket(ticket));
            applicationContext.getBeanFactory().destroyBean(registry);
            verify(instance, never()).shutdown();
        }

        @RepeatedTest(1)
        void verifyRememberMeTimeToLive() throws Throwable {
            val policy = new RememberMeDelegatingExpirationPolicy();
            policy.addPolicy(RememberMeDelegatingExpirationPolicy.POLICY_NAME_REMEMBER_ME, new HardTimeoutExpirationPolicy(TimeUnit.DAYS.toSeconds(30)));
            policy.addPolicy(BaseDelegatingExpirationPolicy.POLICY_NAME_DEFAULT, new HardTimeoutExpirationPolicy(5));
            val authentication = CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString(),
                Map.of(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, List.of(Boolean.TRUE)));
            val ticketGrantingTicket = new TicketGrantingTicketImpl(new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY)
                .getNewTicketId(TicketGrantingTicket.PREFIX), authentication, policy);
            newTicketRegistry.addTicket(ticketGrantingTicket);
            val entry = getTicketEntry(ticketGrantingTicket);
            assertTrue(entry.getTtl() > TimeUnit.DAYS.toMillis(29));
            assertTrue(entry.getExpirationTime() > System.currentTimeMillis() + TimeUnit.DAYS.toMillis(29));
            newTicketRegistry.deleteTicket(ticketGrantingTicket);
        }

        @RepeatedTest(1)
        void verifyUpdateKeepsRemainingTimeToLive() throws Throwable {
            val policy = new HardTimeoutExpirationPolicy(100);
            policy.setClock(Clock.offset(Clock.systemUTC(), Duration.ofMinutes(-1)));
            val ticketGrantingTicket = new TicketGrantingTicketImpl(new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY)
                .getNewTicketId(TicketGrantingTicket.PREFIX), CoreAuthenticationTestUtils.getAuthentication(), policy);
            policy.setClock(Clock.systemUTC());
            newTicketRegistry.addTicket(ticketGrantingTicket);
            assertTrue(getTicketEntry(ticketGrantingTicket).getTtl() <= TimeUnit.SECONDS.toMillis(41));
            newTicketRegistry.updateTicket(ticketGrantingTicket);
            val entry = getTicketEntry(ticketGrantingTicket);
            assertTrue(entry.getTtl() > 0);
            assertTrue(entry.getTtl() <= TimeUnit.SECONDS.toMillis(41));
            newTicketRegistry.deleteTicket(ticketGrantingTicket);
        }

        @RepeatedTest(1)
        void verifyStreamPagesAcrossTicketMaps() throws Throwable {
            val catalog = new DefaultTicketCatalog();
            val ticketGrantingTicketDefinition = new DefaultTicketDefinition(TicketGrantingTicketImpl.class,
                TicketGrantingTicket.class, TicketGrantingTicket.PREFIX, 0);
            ticketGrantingTicketDefinition.getProperties().setStorageName("tgt-" + UUID.randomUUID());
            catalog.register(ticketGrantingTicketDefinition);
            val transientTicketDefinition = new DefaultTicketDefinition(TransientSessionTicketImpl.class,
                TransientSessionTicket.class, TransientSessionTicket.PREFIX, 1);
            transientTicketDefinition.getProperties().setStorageName("tst-" + UUID.randomUUID());
            catalog.register(transientTicketDefinition);

            val registry = new HazelcastTicketRegistry(CipherExecutor.noOp(), ticketSerializationManager, catalog,
                applicationContext, hazelcastInstance, casProperties.getTicket().getRegistry().getHazelcast());
            for (var i = 0; i < 3; i++) {
                registry.addTicket(new TicketGrantingTicketImpl(new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY)
                    .getNewTicketId(TicketGrantingTicket.PREFIX), CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE));
            }
            for (var i = 0; i < 4; i++) {
                registry.addTicket(new TransientSessionTicketImpl(TransientSessionTicket.PREFIX + '-' + UUID.randomUUID(),
                    NeverExpiresExpirationPolicy.INSTANCE, RegisteredServiceTestUtils.getService(), new HashMap<>()));
            }

            val ticketIds = new HashSet<String>();
            for (var from = 0; from < 9; from += 3) {
                val page = registry.stream(TicketRegistryStreamCriteria.builder().from(from).count(3).build()).map(Ticket::getId).toList();
                assertEquals(from < 6 ? 3 : 1, page.size());
                ticketIds.addAll(page);
            }
            assertEquals(7, ticketIds.size());
            assertEquals(3, registry.stream(TicketRegistryStreamCriteria.builder().from(2).count(3).build()).count());
            assertEquals(2, registry.stream(TicketRegistryStreamCriteria.builder().from(5).build()).count());
            assertEquals(7, registry.stream().count());

            assertEquals(7, registry.deleteAll());
            hazelcastInstance.getMap(ticketGrantingTicketDefinition.getProperties().getStorageName()).destroy();
            hazelcastInstance.getMap(transientTicketDefinition.getProperties().getStorageName()).destroy();
        }

        @RepeatedTest(1)
        void verifySessionsWithAttributeValuesThatAreNotQueries() throws Throwable {
            val registry = new HazelcastTicketRegistry(CipherExecutor.noOp(), ticketSerializationManager, ticketCatalog,
                applicationContext, hazelcastInstance, casProperties.getTicket().getRegistry().getHazelcast());
            val sessionId = "O'Brien') OR (1=1 " + UUID.randomUUID();
            val ticketGrantingTicket = new TicketGrantingTicketImpl(new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY)
                .getNewTicketId(TicketGrantingTicket.PREFIX), CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString(),
                Map.of("sid", List.of(sessionId))), NeverExpiresExpirationPolicy.INSTANCE);
            registry.addTicket(ticketGrantingTicket);

            val sessions = registry.getSessionsWithAttributes(Map.of("sid", List.of(sessionId))).toList();
            assertEquals(1, sessions.size());
            assertEquals(ticketGrantingTicket.getId(), sessions.getFirst().getId());
            assertEquals(0, registry.getSessionsWithAttributes(Map.of("sid", List.of("O'Brien"))).count());
            assertEquals(0, registry.getSessionsWithAttributes(Map.of()).count());
            registry.deleteTicket(ticketGrantingTicket);
        }

        private EntryView<String, HazelcastTicketDocument> getTicketEntry(final Ticket ticket) {
            val storageName = ticketCatalog.find(ticket).getProperties().getStorageName();
            return hazelcastInstance.<String, HazelcastTicketDocument>getMap(storageName)
                .getEntryView(newTicketRegistry.digestIdentifier(ticket.getId()));
        }

        @RepeatedTest(1)
        void verifyBadTicketInCatalog() {
            val ticket = new MockTicketGrantingTicket("casuser");

            val instance = mock(HazelcastInstance.class);
            when(instance.getMap(anyString())).thenThrow(new RuntimeException());

            val catalog = mock(TicketCatalog.class);
            val defn = new DefaultTicketDefinition(ticket.getClass(), TicketGrantingTicket.class, ticket.getPrefix(), 0);
            defn.getProperties().setStorageName("Tickets");
            when(catalog.find(any(Ticket.class))).thenReturn(defn);
            val registry = new HazelcastTicketRegistry(CipherExecutor.noOp(), ticketSerializationManager, catalog,
                applicationContext, instance, casProperties.getTicket().getRegistry().getHazelcast());
            assertDoesNotThrow(() -> registry.addTicket(ticket));
            assertNull(registry.getTicket(ticket.getId()));
        }
    }

    @Nested
    @SpringBootTest(classes = {
        CasHazelcastTicketRegistryAutoConfiguration.class,
        BaseTicketRegistryTests.SharedTestConfiguration.class
    })
    @TestPropertySource(
        properties = {
            "cas.ticket.registry.hazelcast.cluster.network.port=5705",
            "cas.ticket.registry.hazelcast.cluster.network.tcpip-enabled=false",
            "cas.ticket.registry.hazelcast.cluster.core.instance-name=loadtestinstance"
        })
    class LoadTests {
        private static final int COUNT = 50_000;

        @Autowired
        @Qualifier(TicketRegistry.BEAN_NAME)
        private TicketRegistry ticketRegistry;

        @RepeatedTest(1)
        @Tag("TicketRegistryTestWithEncryption")
        void verifyLargeDataset() {
            val username = UUID.randomUUID().toString();
            val ticketGrantingTickets = Stream.generate(() -> {
                val tgtId = new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY).getNewTicketId(TicketGrantingTicket.PREFIX);
                return new TicketGrantingTicketImpl(tgtId, CoreAuthenticationTestUtils.getAuthentication(username), NeverExpiresExpirationPolicy.INSTANCE);
            }).limit(COUNT);

            val stopwatch = new StopWatch();
            stopwatch.start();
            ticketRegistry.addTicket(ticketGrantingTickets);

            assertEquals(COUNT, ticketRegistry.getTickets().size());
            stopwatch.stop();
            LOGGER.info("Added and read back [{}] tickets in [{}]s", COUNT, stopwatch.getTime(TimeUnit.SECONDS));

            stopwatch.reset();
            stopwatch.start();
            var results = ticketRegistry
                .stream()
                .filter(ticket -> ticket instanceof TicketGrantingTicket && !ticket.isExpired())
                .map(TicketGrantingTicket.class::cast)
                .filter(ticket -> username.equals(ticket.getAuthentication().getPrincipal().getId()))
                .toList();
            assertFalse(results.isEmpty());
            stopwatch.stop();
            LOGGER.info("Streamed and filtered [{}] tickets in [{}]s", COUNT, stopwatch.getTime(TimeUnit.SECONDS));
        }
    }
}
