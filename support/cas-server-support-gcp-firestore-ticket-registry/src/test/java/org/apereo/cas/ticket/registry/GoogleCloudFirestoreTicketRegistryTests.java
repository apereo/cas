package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasGoogleCloudFirestoreTicketRegistryAutoConfiguration;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.util.TicketGrantingTicketIdGenerator;
import org.apereo.cas.util.lock.LockRepository;
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider;
import com.google.auth.ApiKeyCredentials;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.spring.autoconfigure.core.GcpContextAutoConfiguration;
import com.google.cloud.spring.autoconfigure.firestore.GcpFirestoreAutoConfiguration;
import com.google.cloud.spring.autoconfigure.firestore.GcpFirestoreProperties;
import com.google.cloud.spring.core.GcpProjectIdProvider;
import com.google.firestore.v1.FirestoreGrpc;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GoogleCloudFirestoreTicketRegistryTests}.
 * <p>
 * The two nested classes are the encrypted and the unencrypted run. Each settles its cipher through
 * its own configuration, so that whether tickets are encrypted is a property of the context rather
 * than something written onto the shared registry bean before every test method, which methods
 * running side by side cannot agree on. Each is given a Firestore project and database of its own so
 * that neither can read back what the other wrote under different keys.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("GCP")
class GoogleCloudFirestoreTicketRegistryTests {

    @Nested
    @Tag("TicketRegistryTestWithoutEncryption")
    @Import(GoogleCloudFirestoreTicketRegistryTests.GoogleCloudFirestoreTestConfiguration.class)
    @TestPropertySource(properties = "spring.cloud.gcp.firestore.project-id=apereo-cas-gcp-plain")
    class WithoutEncryption extends BaseGoogleCloudFirestoreTicketRegistryTests {
    }

    @Nested
    @Tag("TicketRegistryTestWithEncryption")
    @Import(GoogleCloudFirestoreTicketRegistryTests.GoogleCloudFirestoreTestConfiguration.class)
    @TestPropertySource(properties = {
        "spring.cloud.gcp.firestore.project-id=apereo-cas-gcp-crypto",
        "cas.ticket.registry.google-cloud-firestore.crypto.enabled=true"
    })
    class WithEncryption extends BaseGoogleCloudFirestoreTicketRegistryTests {
    }

    @Getter
    @ImportAutoConfiguration({
        CasGoogleCloudFirestoreTicketRegistryAutoConfiguration.class,
        GcpFirestoreAutoConfiguration.class,
        GcpContextAutoConfiguration.class
    })
    @TestPropertySource(properties = {
        "spring.cloud.gcp.firestore.emulator.enabled=true",
        "spring.cloud.gcp.firestore.host-port=127.0.0.1:9980"
    })
    @Tag("SkipClearingTicketRegistry")
    abstract static class BaseGoogleCloudFirestoreTicketRegistryTests extends BaseTicketRegistryTests {
        private static final int COUNT = 100;

        @Autowired
        @Qualifier(TicketRegistry.BEAN_NAME)
        private TicketRegistry newTicketRegistry;

        @Override
        protected boolean isCipherExecutorOwnedByContext() {
            return true;
        }

        /**
         * Adding a batch of sessions in bulk must make every one of them retrievable. The count is
         * asked for by principal rather than for the registry as a whole, which would answer for
         * every test sharing the database as well.
         */
        @RepeatedTest(1)
        void verifyLargeDataset() {
            val principal = UUID.randomUUID().toString();
            val authentication = CoreAuthenticationTestUtils.getAuthentication(principal);
            val ticketGrantingTickets = Stream.generate(() -> {
                val tgtId = new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY)
                    .getNewTicketId(TicketGrantingTicket.PREFIX);
                return new TicketGrantingTicketImpl(tgtId, authentication, NeverExpiresExpirationPolicy.INSTANCE);
            }).limit(COUNT);
            getNewTicketRegistry().addTicket(ticketGrantingTickets);
            assertEquals(COUNT, getNewTicketRegistry().countSessionsFor(principal));
        }

        /**
         * A batch of expired sessions must all be removed by one pass of the cleaner. The cleaner
         * works on the registry as a whole and so may carry off tickets other tests have let expire
         * too, which is why the count it reports is asserted as a floor rather than an equality.
         *
         * @throws Throwable in case of failure
         */
        @RepeatedTest(1)
        void verifyCleanLargeBatch() throws Throwable {
            val principal = UUID.randomUUID().toString();
            val authentication = CoreAuthenticationTestUtils.getAuthentication(principal);
            var lastTicket = (TicketGrantingTicketImpl) null;
            for (var i = 0; i < COUNT; i++) {
                val tgtId = new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY)
                    .getNewTicketId(TicketGrantingTicket.PREFIX);
                lastTicket = new TicketGrantingTicketImpl(tgtId, authentication, new HardTimeoutExpirationPolicy(1));
                getNewTicketRegistry().addTicket(lastTicket);
            }
            val newestTicket = lastTicket;
            await().atMost(Duration.ofSeconds(30)).until(newestTicket::isExpired);
            val cleaner = new DefaultTicketRegistryCleaner(LockRepository.noOp(), applicationContext, getNewTicketRegistry());
            val cleaned = cleaner.clean();
            assertTrue(cleaned >= COUNT, () -> "Cleaned %s tickets, expected at least %s".formatted(cleaned, COUNT));
        }

        /**
         * Removing every session held for one principal must leave none behind for that principal.
         * The inherited version empties the registry and asserts it reports nothing at all, which
         * can only be true of a registry no other test is using.
         *
         * @throws Throwable in case of failure
         */
        @Override
        @RepeatedTest(2)
        void verifyGetTicketsIsZero() throws Throwable {
            val principal = UUID.randomUUID().toString();
            addSessionFor(principal);
            getNewTicketRegistry().deleteTicketsFor(principal);
            assertEquals(0, getNewTicketRegistry().countSessionsFor(principal));
        }

        /**
         * Removal reports how many it removed. Asked for one principal rather than for the whole
         * registry, which the inherited version empties and counts.
         *
         * @throws Throwable in case of failure
         */
        @Override
        @RepeatedTest(2)
        void verifyDeleteAllExistingTickets() throws Throwable {
            val principal = UUID.randomUUID().toString();
            addSessionFor(principal);
            assertEquals(1, getNewTicketRegistry().deleteTicketsFor(principal));
            assertEquals(0, getNewTicketRegistry().countSessionsFor(principal));
        }

        private void addSessionFor(final String principal) throws Throwable {
            val ticketGrantingTicketId = new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY)
                .getNewTicketId(TicketGrantingTicket.PREFIX);
            getNewTicketRegistry().addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
                CoreAuthenticationTestUtils.getAuthentication(principal), NeverExpiresExpirationPolicy.INSTANCE));
        }
    }

    @TestConfiguration(value = "GoogleCloudFirestoreTestConfiguration", proxyBeanMethods = false)
    static class GoogleCloudFirestoreTestConfiguration {

        @Bean
        public GcpProjectIdProvider gcpProjectIdProvider(final GcpFirestoreProperties properties) {
            return properties::getProjectId;
        }

        @Bean
        public FirestoreGrpc.FirestoreStub firestoreGrpcStub() {
            return mock(FirestoreGrpc.FirestoreStub.class);
        }

        @Bean
        public FirestoreOptions firestoreOptions(final GcpFirestoreProperties properties) {
            return FirestoreOptions.getDefaultInstance().toBuilder()
                .setCredentials(ApiKeyCredentials.create(UUID.randomUUID().toString()))
                .setProjectId(properties.getProjectId())
                .setChannelProvider(InstantiatingGrpcChannelProvider.newBuilder()
                    .setEndpoint(properties.getHostPort())
                    .build())
                .setEmulatorHost(properties.getHostPort())
                .setDatabaseId(UUID.randomUUID().toString())
                .build();
        }
    }
}
