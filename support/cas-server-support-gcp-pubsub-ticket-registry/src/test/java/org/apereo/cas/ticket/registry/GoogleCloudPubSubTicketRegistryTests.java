package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.GoogleCloudPubSubTicketRegistryConfiguration;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.registry.pubsub.commands.AddTicketMessageQueueCommand;
import org.apereo.cas.ticket.registry.pubsub.commands.BaseMessageQueueCommand;
import org.apereo.cas.ticket.registry.pubsub.queue.QueueableTicketRegistryMessageReceiver;
import org.apereo.cas.util.PublisherIdentifier;

import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.spring.core.GcpProjectIdProvider;
import com.google.cloud.spring.pubsub.PubSubAdmin;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.core.publisher.PubSubPublisherTemplate;
import com.google.cloud.spring.pubsub.core.subscriber.PubSubSubscriberTemplate;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.converter.PubSubMessageConverter;
import com.google.pubsub.v1.ProjectName;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.Topic;
import com.google.pubsub.v1.TopicName;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GoogleCloudPubSubTicketRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("GCP")
@Import({
    GoogleCloudPubSubTicketRegistryTests.GoogleCloudTestConfiguration.class,
    GoogleCloudPubSubTicketRegistryConfiguration.class
})
@Getter
public class GoogleCloudPubSubTicketRegistryTests extends BaseTicketRegistryTests {
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
    public void verifyConverter() {
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
    public void verifyTicketConsumer() throws Exception {
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
    public static class GoogleCloudTestConfiguration {
        @Bean
        public GcpProjectIdProvider gcpProjectIdProvider() {
            return () -> UUID.randomUUID().toString();
        }

        @Bean
        public PubSubAdmin pubSubAdmin(final GcpProjectIdProvider gcpProjectIdProvider) {
            val topicAdminClient = mock(TopicAdminClient.class);
            val listResponse = mock(TopicAdminClient.ListTopicsPagedResponse.class);
            when(listResponse.iterateAll()).thenReturn(List.of());

            val topic = Topic.newBuilder().setName(GoogleCloudTicketRegistryQueuePublisher.QUEUE_TOPIC).build();
            when(topicAdminClient.createTopic(anyString())).thenReturn(topic);
            when(topicAdminClient.createTopic(any(TopicName.class))).thenReturn(topic);

            when(topicAdminClient.listTopics(any(ProjectName.class))).thenReturn(listResponse);

            val subscriptionAdminClient = mock(SubscriptionAdminClient.class);
            val listSubsResponse = mock(SubscriptionAdminClient.ListSubscriptionsPagedResponse.class);
            when(listSubsResponse.iterateAll()).thenReturn(List.of());
            when(subscriptionAdminClient.listSubscriptions(any(ProjectName.class))).thenReturn(listSubsResponse);
            val subscription = Subscription.newBuilder().setName("MySubscription").build();
            when(subscriptionAdminClient.createSubscription(any(Subscription.class))).thenReturn(subscription);

            return new PubSubAdmin(gcpProjectIdProvider, topicAdminClient, subscriptionAdminClient);
        }

        @Bean
        public PubSubTemplate pubSubTemplate() throws Exception {
            val apiFuture = mock(CompletableFuture.class);
            when(apiFuture.get()).thenReturn(Boolean.TRUE.toString());
            val pubSubPublisherTemplate = mock(PubSubPublisherTemplate.class);
            when(pubSubPublisherTemplate.publish(anyString(), any(PubsubMessage.class))).thenReturn(apiFuture);
            when(pubSubPublisherTemplate.publish(anyString(), any(), anyMap())).thenReturn(apiFuture);

            val pubSubSubscriberTemplate = mock(PubSubSubscriberTemplate.class);

            return new PubSubTemplate(pubSubPublisherTemplate, pubSubSubscriberTemplate);
        }
    }
}
