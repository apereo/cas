package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreEventsAutoConfiguration;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.mock.MockServiceTicket;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.events.logout.CasRequestSingleLogoutEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketDestroyedEvent;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.DefaultTicketCatalog;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.util.cipher.DefaultTicketCipherExecutor;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test case to test the DefaultTicketRegistry based on test cases to test all
 * Ticket Registries.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("Tickets")
class DefaultTicketRegistryTests {

    @Nested
    class DefaultTests extends BaseTicketRegistryTests {
        @Override
        public TicketRegistry getNewTicketRegistry() {
            return new DefaultTicketRegistry(mock(TicketSerializationManager.class), new DefaultTicketCatalog(),
                    mock(ConfigurableApplicationContext.class));
        }

        @RepeatedTest(1)
        void verifyCountsUnknown() throws Throwable {
            val registry = mock(DefaultTicketRegistry.class);
            when(registry.stream()).thenThrow(IllegalArgumentException.class);
            when(registry.sessionCount()).thenCallRealMethod();
            when(registry.serviceTicketCount()).thenCallRealMethod();
            assertEquals(Long.MIN_VALUE, registry.sessionCount());
            assertEquals(Long.MIN_VALUE, registry.serviceTicketCount());
        }

        @RepeatedTest(1)
        void verifyRegistryQuery() throws Throwable {
            val user = UUID.randomUUID().toString();
            val tgt = new MockTicketGrantingTicket(user);
            val st = new MockServiceTicket("ST-123456", RegisteredServiceTestUtils.getService(), tgt);
            val registry = getNewTicketRegistry();
            registry.addTicket(tgt);
            registry.addTicket(st);

            val count = registry.countSessionsFor(user);
            assertEquals(1, count);
            assertNotEquals(0, registry.query(TicketRegistryQueryCriteria.builder()
                    .type(TicketGrantingTicket.PREFIX).build()).size());
        }

        @RepeatedTest(2)
        void verifyCountForService() throws Throwable {
            val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
            val registry = getNewTicketRegistry();
            val user = UUID.randomUUID().toString();

            for (var i = 0; i < 5; i++) {
                val tgt = new MockTicketGrantingTicket(user);
                val st = tgt.grantServiceTicket(service, TicketTrackingPolicy.noOp());
                registry.addTicket(st);
                registry.updateTicket(tgt);
            }
            val count = registry.countTicketsFor(service);
            assertEquals(5, count);
        }


        @RepeatedTest(1)
        void verifyEncodeFails() throws Throwable {
            val cipher = new DefaultTicketCipherExecutor(null, null,
                    "AES", 512, 16, "webflow");
            val reg = new DefaultTicketRegistry(cipher, mock(TicketSerializationManager.class), new DefaultTicketCatalog(),
                    mock(ConfigurableApplicationContext.class));
            assertNull(reg.encodeTicket(null));
            assertNotNull(reg.decodeTicket(mock(Ticket.class)));
        }

        @RepeatedTest(1)
        void verifyGetExpiredTicketEventsSend() throws Throwable {
            val applicationContext = mock(ConfigurableApplicationContext.class);
            val registry = new DefaultTicketRegistry(mock(TicketSerializationManager.class), new DefaultTicketCatalog(),
                    applicationContext);
            val originalAuthn = CoreAuthenticationTestUtils.getAuthentication();
            val ticketGrantingTicket = new TicketGrantingTicketImpl(TestTicketIdentifiers.generate().ticketGrantingTicketId(),
                    originalAuthn, new HardTimeoutExpirationPolicy(1));
            registry.addTicket(ticketGrantingTicket);
            Thread.sleep(1500);
            val tgtId = ticketGrantingTicket.getId();
            val tgt = registry.getTicket(tgtId);
            assertNull(tgt);
            val internalTgt = registry.getMapInstance().get(tgtId);
            assertNull(internalTgt);
            verify(applicationContext).publishEvent(any(CasRequestSingleLogoutEvent.class));
            verify(applicationContext).publishEvent(any(CasTicketGrantingTicketDestroyedEvent.class));
        }
    }

    @SpringBootTest(classes = {
            BaseTicketRegistryTests.SharedTestConfiguration.class,
            LogoutTests.LogoutManagerTestConfiguration.class
        },
        properties = {
            "cas.ticket.tgt.core.only-track-most-recent-session=false",
            "cas.ticket.registry.cleaner.schedule.enabled=false"
        })
    @ImportAutoConfiguration(CasCoreEventsAutoConfiguration.class)
    @ExtendWith(CasTestExtension.class)
    @Nested
    class LogoutTests {

        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @TestConfiguration(value = "LogoutManagerTestConfiguration", proxyBeanMethods = false)
        public static class LogoutManagerTestConfiguration {

            private static int NB_CALLS;

            public static int getNbCalls() {
                return NB_CALLS;
            }

            @Bean
            public LogoutManager logoutManager() {
                return context -> {
                    NB_CALLS++;
                    return List.of();
                };
            }
        }

        @RepeatedTest(1)
        void verifyGetExpiredTicketLogoutPerformed() throws Throwable {
            val registry = new DefaultTicketRegistry(mock(TicketSerializationManager.class), new DefaultTicketCatalog(),
                    applicationContext);
            val originalAuthn = CoreAuthenticationTestUtils.getAuthentication();
            val ticketGrantingTicket = new TicketGrantingTicketImpl(BaseTicketRegistryTests.TestTicketIdentifiers.generate().ticketGrantingTicketId(),
                    originalAuthn, new HardTimeoutExpirationPolicy(1));
            registry.addTicket(ticketGrantingTicket);
            Thread.sleep(1500);
            val tgtId = ticketGrantingTicket.getId();
            val tgt = registry.getTicket(tgtId);
            assertNull(tgt);
            val internalTgt = registry.getMapInstance().get(tgtId);
            assertNull(internalTgt);
            await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> assertTrue(LogoutManagerTestConfiguration.getNbCalls() > 0));
        }
    }
}
