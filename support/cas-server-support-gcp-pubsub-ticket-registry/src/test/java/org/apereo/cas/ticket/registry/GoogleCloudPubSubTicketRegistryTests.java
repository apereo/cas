package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasGoogleCloudPubSubTicketRegistryAutoConfiguration;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.registry.pubsub.commands.AddTicketMessageQueueCommand;
import org.apereo.cas.ticket.registry.pubsub.commands.BaseMessageQueueCommand;
import org.apereo.cas.ticket.registry.pubsub.queue.QueueableTicketRegistryMessageReceiver;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.cloud.spring.autoconfigure.core.GcpContextAutoConfiguration;
import com.google.cloud.spring.autoconfigure.pubsub.GcpPubSubAutoConfiguration;
import com.google.cloud.spring.autoconfigure.pubsub.GcpPubSubProperties;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.converter.PubSubMessageConverter;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.Topic;
import io.grpc.ManagedChannelBuilder;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GoogleCloudPubSubTicketRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("GCP")
@Import(GoogleCloudPubSubTicketRegistryTests.GoogleCloudTestConfiguration.class)
@ImportAutoConfiguration({
    GcpContextAutoConfiguration.class,
    GcpPubSubAutoConfiguration.class,
    CasGoogleCloudPubSubTicketRegistryAutoConfiguration.class
})
@Getter
@TestPropertySource(properties = {
    "spring.cloud.gcp.project-id=apereo-cas-gcp",
    
    "spring.cloud.gcp.pubsub.publisher.enable-message-ordering=true",
    "spring.cloud.gcp.pubsub.publisher.endpoint=localhost:8085",

    "spring.cloud.gcp.pubsub.emulator-host=localhost:8085",

    "spring.cloud.gcp.pubsub.health.lag-threshold=5",
    "spring.cloud.gcp.pubsub.health.backlog-threshold=3",
    "spring.cloud.gcp.pubsub.health.look-up-interval=2"
})
@EnabledIfListeningOnPort(port = 8085)
class GoogleCloudPubSubTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry newTicketRegistry;

    @Autowired
    @Qualifier("pubSubMessageConverter")
    private PubSubMessageConverter pubSubMessageConverter;

    @Autowired
    @Qualifier("messageQueueTicketRegistryReceiver")
    private QueueableTicketRegistryMessageReceiver messageQueueTicketRegistryReceiver;

    @RepeatedTest(1)
    @Tag("DisableTicketRegistryTestWithEncryption")
    void verifyConverter() {
        val cmd = new AddTicketMessageQueueCommand(new PublisherIdentifier(),
            new TicketGrantingTicketImpl(UUID.randomUUID().toString(),
                CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE));
        val message = pubSubMessageConverter.toPubSubMessage(cmd, Map.of());
        assertNotNull(message);
        val foundCmd = pubSubMessageConverter.fromPubSubMessage(message, BaseMessageQueueCommand.class);
        assertNotNull(foundCmd);
    }

    @RepeatedTest(1)
    @Tag("DisableTicketRegistryTestWithEncryption")
    void verifyTicketConsumer() throws Throwable {
        val objectToSend = new AddTicketMessageQueueCommand(new PublisherIdentifier(),
            new TicketGrantingTicketImpl(UUID.randomUUID().toString(),
                CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE));
        val originalMessage = pubSubMessageConverter.toPubSubMessage(objectToSend, Map.of());

        val topic = Topic.newBuilder().setName(UUID.randomUUID().toString()).build();
        val subscription = Subscription.newBuilder().setName(UUID.randomUUID().toString()).build();
        val cmd = new GoogleCloudTicketRegistryMessageQueueConsumer(topic, subscription,
            messageQueueTicketRegistryReceiver, pubSubMessageConverter);
        val message = mock(BasicAcknowledgeablePubsubMessage.class);
        when(message.getProjectSubscriptionName())
            .thenReturn(ProjectSubscriptionName.of("project", "subscription"));
        when(message.getPubsubMessage()).thenReturn(originalMessage);

        val apiFuture = mock(CompletableFuture.class);
        when(apiFuture.get()).thenReturn(Boolean.TRUE.toString());
        when(message.ack()).thenReturn(apiFuture);
        when(message.nack()).thenReturn(apiFuture);

        assertDoesNotThrow(() -> cmd.accept(message));

        when(message.getProjectSubscriptionName()).thenReturn(null);
        assertDoesNotThrow(() -> cmd.accept(message));
    }

    @TestConfiguration(value = "GoogleCloudPubSubTestConfiguration", proxyBeanMethods = false)
    static class GoogleCloudTestConfiguration {

        @Bean
        public TransportChannelProvider publisherTransportChannelProvider(final GcpPubSubProperties properties) {
            val channel = ManagedChannelBuilder.forTarget(properties.getEmulatorHost()).usePlaintext().build();
            return FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
        }

        @Bean
        public TransportChannelProvider subscriberTransportChannelProvider(final GcpPubSubProperties properties) {
            val channel = ManagedChannelBuilder.forTarget(properties.getEmulatorHost()).usePlaintext().build();
            return FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
        }

        @Bean
        public TopicAdminSettings topicAdminSettings(
            final GcpPubSubProperties properties) throws Exception {
            val channel = ManagedChannelBuilder.forTarget(properties.getEmulatorHost()).usePlaintext().build();
            val channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
            return TopicAdminSettings.newBuilder()
                .setCredentialsProvider(NoCredentialsProvider.create())
                .setTransportChannelProvider(channelProvider)
                .build();
        }

        @Bean
        public SubscriptionAdminClient subscriptionAdminClient(
            final GcpPubSubProperties properties) throws Exception {
            val channel = ManagedChannelBuilder.forTarget(properties.getEmulatorHost()).usePlaintext().build();
            val channelProvider =
                FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
            return SubscriptionAdminClient.create(
                SubscriptionAdminSettings.newBuilder()
                    .setCredentialsProvider(NoCredentialsProvider.create())
                    .setTransportChannelProvider(channelProvider)
                    .build());
        }
    }
}
