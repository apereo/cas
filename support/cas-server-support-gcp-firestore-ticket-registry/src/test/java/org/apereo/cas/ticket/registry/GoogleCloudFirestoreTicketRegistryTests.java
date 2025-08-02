package org.apereo.cas.ticket.registry;

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
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GoogleCloudFirestoreTicketRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Tag("GCP")
@Import(GoogleCloudFirestoreTicketRegistryTests.GoogleCloudFirestoreTestConfiguration.class)
@ImportAutoConfiguration({
    CasGoogleCloudFirestoreTicketRegistryAutoConfiguration.class,
    GcpFirestoreAutoConfiguration.class,
    GcpContextAutoConfiguration.class
})
@TestPropertySource(properties = {
    "spring.cloud.gcp.firestore.project-id=apereo-cas-gcp",

    "spring.cloud.gcp.firestore.emulator.enabled=true",
    "spring.cloud.gcp.firestore.host-port=127.0.0.1:9980"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GoogleCloudFirestoreTicketRegistryTests extends BaseTicketRegistryTests {
    private static final int COUNT = 100;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry newTicketRegistry;

    @RepeatedTest(1)
    @Tag("TicketRegistryTestWithEncryption")
    void verifyLargeDataset() {
        val ticketGrantingTickets = Stream.generate(() -> {
            val tgtId = new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY)
                .getNewTicketId(TicketGrantingTicket.PREFIX);
            return new TicketGrantingTicketImpl(tgtId,
                CoreAuthenticationTestUtils.getAuthentication(),
                NeverExpiresExpirationPolicy.INSTANCE);
        }).limit(COUNT);

        val stopwatch = new StopWatch();
        stopwatch.start();
        newTicketRegistry.addTicket(ticketGrantingTickets);
        val size = newTicketRegistry.getTickets().size();
        stopwatch.stop();
        assertEquals(COUNT, size);
        val time = stopwatch.getTime(TimeUnit.SECONDS);
        assertTrue(time <= 20);
    }

    @RepeatedTest(1)
    @Tag("TicketRegistryTestWithEncryption")
    @Order(0)
    void verifyCleanLargeBatch() throws Throwable {
        newTicketRegistry.deleteAll();
        for (var i = 0; i < COUNT; i++) {
            val tgtId = new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY)
                .getNewTicketId(TicketGrantingTicket.PREFIX);
            val tgt = new TicketGrantingTicketImpl(tgtId,
                CoreAuthenticationTestUtils.getAuthentication(),
                new HardTimeoutExpirationPolicy(1));
            newTicketRegistry.addTicket(tgt);
        }
        Thread.sleep(Duration.ofSeconds(1));
        val cleaner = new DefaultTicketRegistryCleaner(LockRepository.noOp(), applicationContext, newTicketRegistry);

        val stopwatch = new StopWatch();
        stopwatch.start();
        val cleaned = cleaner.clean();
        stopwatch.stop();
        val time = stopwatch.getTime(TimeUnit.SECONDS);
        assertTrue(time <= 5);
        assertEquals(COUNT, cleaned);
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
