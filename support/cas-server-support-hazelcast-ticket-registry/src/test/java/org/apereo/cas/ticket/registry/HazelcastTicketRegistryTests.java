package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasHazelcastTicketRegistryAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.DefaultTicketDefinition;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.util.TicketGrantingTicketIdGenerator;
import org.apereo.cas.util.crypto.CipherExecutor;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import lombok.Getter;
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
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link HazelcastTicketRegistry}.
 *
 * @author Dmitriy Kopylenko
 * @since 4.1.0
 */
@Tag("Hazelcast")
@ExtendWith(CasTestExtension.class)
class HazelcastTicketRegistryTests {
    @Nested
    @Getter
    @ImportAutoConfiguration(CasHazelcastTicketRegistryAutoConfiguration.class)
    @Import(BaseTicketRegistryTests.SharedTestConfiguration.class)
    @TestPropertySource(
        properties = {
            "cas.ticket.registry.hazelcast.core.enable-jet=false",
            "cas.ticket.registry.hazelcast.cluster.network.port-auto-increment=false",
            "cas.ticket.registry.hazelcast.cluster.network.port=5707",
            "cas.ticket.registry.hazelcast.cluster.core.instance-name=testjetlessinstance"
        })
    class JetlessTests extends BaseTicketRegistryTests {
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
            "cas.ticket.registry.hazelcast.cluster.network.port-auto-increment=false",
            "cas.ticket.registry.hazelcast.cluster.network.port=5703",
            "cas.ticket.registry.hazelcast.cluster.core.instance-name=testlocalhostinstance"
        })
    class DefaultTests extends BaseTicketRegistryTests {
        @Autowired
        @Qualifier(TicketRegistry.BEAN_NAME)
        private TicketRegistry newTicketRegistry;

        @Autowired
        private CasConfigurationProperties casProperties;

        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @RepeatedTest(1)
        void verifyBadExpPolicyValue() throws Throwable {
            val ticket = new MockTicketGrantingTicket("casuser");

            val instance = mock(HazelcastInstance.class);
            val myMap = mock(IMap.class);
            when(instance.getMap(anyString())).thenReturn(myMap);
            try (val registry = new HazelcastTicketRegistry(CipherExecutor.noOp(), ticketSerializationManager, ticketCatalog,
                    applicationContext, instance, casProperties.getTicket().getRegistry().getHazelcast())) {
                ticket.setExpirationPolicy(new HardTimeoutExpirationPolicy(-1));
                assertDoesNotThrow(() -> registry.addTicket(ticket));
                assertDoesNotThrow(registry::shutdown);
            }
        }

        @RepeatedTest(1)
        void verifyBadTicketInCatalog() throws Throwable {
            val ticket = new MockTicketGrantingTicket("casuser");

            val instance = mock(HazelcastInstance.class);
            when(instance.getMap(anyString())).thenThrow(new RuntimeException());

            val catalog = mock(TicketCatalog.class);
            val defn = new DefaultTicketDefinition(ticket.getClass(), TicketGrantingTicket.class, ticket.getPrefix(), 0);
            defn.getProperties().setStorageName("Tickets");
            when(catalog.find(any(Ticket.class))).thenReturn(defn);
            try (val registry = new HazelcastTicketRegistry(CipherExecutor.noOp(), ticketSerializationManager, catalog,
                    applicationContext, instance, casProperties.getTicket().getRegistry().getHazelcast())) {
                assertDoesNotThrow(() -> registry.addTicket(ticket));
                assertNull(registry.getTicket(ticket.getId()));
            }
        }
    }

    @Nested
    @SpringBootTest(classes = {
        CasHazelcastTicketRegistryAutoConfiguration.class,
        BaseTicketRegistryTests.SharedTestConfiguration.class
    })
    @TestPropertySource(
        properties = {
            "cas.ticket.registry.hazelcast.cluster.network.port-auto-increment=false",
            "cas.ticket.registry.hazelcast.cluster.network.port=5705",
            "cas.ticket.registry.hazelcast.cluster.core.instance-name=loadtestinstance"
        })
    class LoadTests {
        private static final int COUNT = 50_000;

        @Autowired
        @Qualifier(TicketRegistry.BEAN_NAME)
        private TicketRegistry ticketRegistry;

        @RepeatedTest(1)
        @Tag("TicketRegistryTestWithEncryption")
        void verifyLargeDataset() throws Throwable {
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
            var time = stopwatch.getTime(TimeUnit.SECONDS);
            assertTrue(time <= 20);

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
            time = stopwatch.getTime(TimeUnit.SECONDS);
            assertTrue(time <= 20);
        }
    }
}
