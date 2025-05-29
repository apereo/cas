package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.ticket.registry.GoogleCloudPubSubMessageContext;
import org.apereo.cas.ticket.registry.GoogleCloudPubSubMessageConverter;
import org.apereo.cas.ticket.registry.GoogleCloudTicketRegistryMessageQueueConsumer;
import org.apereo.cas.ticket.registry.GoogleCloudTicketRegistryQueuePublisher;
import org.apereo.cas.ticket.registry.pubsub.queue.QueueableTicketRegistryMessagePublisher;
import org.apereo.cas.ticket.registry.pubsub.queue.QueueableTicketRegistryMessageReceiver;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.spring.pubsub.PubSubAdmin;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.support.converter.PubSubMessageConverter;
import com.google.pubsub.v1.DeadLetterPolicy;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.Topic;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link CasGoogleCloudPubSubTicketRegistryAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = false)
@EnableScheduling
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry, module = "gcp")
@AutoConfiguration
@Slf4j
@Lazy(false)
public class CasGoogleCloudPubSubTicketRegistryAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "pubSubMessageConverter")
    public PubSubMessageConverter pubSubMessageConverter(
        @Qualifier(CipherExecutor.BEAN_NAME_TICKET_REGISTRY_CIPHER_EXECUTOR)
        final CipherExecutor defaultTicketRegistryCipherExecutor) {
        return new GoogleCloudPubSubMessageConverter(defaultTicketRegistryCipherExecutor);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public QueueableTicketRegistryMessagePublisher messageQueueTicketRegistryPublisher(
        @Qualifier("pubSubMessageConverter")
        final PubSubMessageConverter pubSubMessageConverter,
        @Qualifier("messageQueueTicketRegistryIdentifier")
        final PublisherIdentifier messageQueueTicketRegistryIdentifier,
        final PubSubTemplate pubSubTemplate) {
        LOGGER.debug("Configuring Google Cloud ticket registry with identifier [{}]", messageQueueTicketRegistryIdentifier);
        pubSubTemplate.setMessageConverter(pubSubMessageConverter);
        return new GoogleCloudTicketRegistryQueuePublisher(pubSubTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(name = "googleCloudPubSubTopics")
    public Map<String, GoogleCloudPubSubMessageContext> googleCloudPubSubTopics(
        @Qualifier("pubSubMessageConverter")
        final PubSubMessageConverter pubSubMessageConverter,
        @Qualifier("messageQueueTicketRegistryReceiver")
        final QueueableTicketRegistryMessageReceiver messageQueueTicketRegistryReceiver,
        final PubSubTemplate pubSubTemplate,
        final PubSubAdmin pubSubAdmin) {

        val googleCloudTopics = new LinkedHashMap<String, GoogleCloudPubSubMessageContext>();

        LOGGER.info("Preparing Google Cloud Pub/Sub topics and subscriptions...");
        val allTopics = pubSubAdmin.listTopics();
        val subscriptions = pubSubAdmin.listSubscriptions();

        val topicName = GoogleCloudTicketRegistryQueuePublisher.QUEUE_TOPIC;
        val subscriptionName = topicName.concat("Subscription");

        val deadLetterTopic = findTopicByName(allTopics, GoogleCloudTicketRegistryQueuePublisher.DEAD_LETTER_TOPIC)
            .orElseGet(() -> pubSubAdmin.createTopic(GoogleCloudTicketRegistryQueuePublisher.DEAD_LETTER_TOPIC));
        val topic = findTopicByName(allTopics, topicName)
            .orElseGet(() -> Objects.requireNonNull(pubSubAdmin.createTopic(topicName)));
        val subscription = getOrCreateSubscription(pubSubAdmin, subscriptions, subscriptionName, topic, deadLetterTopic);
        LOGGER.debug("Created subscription [{}] for topic [{}]", subscription.getName(), topic.getName());
        val subscriber = subscribeToTopic(messageQueueTicketRegistryReceiver, pubSubTemplate, topic, subscription, pubSubMessageConverter);
        val context = new GoogleCloudPubSubMessageContext(topic, subscription, subscriber);
        googleCloudTopics.put(context.topic().getName(), context);
        return googleCloudTopics;
    }

    private static Optional<Topic> findTopicByName(final List<Topic> allTopics, final String topicName) {
        return allTopics
            .stream()
            .filter(topic -> topic.getName().contains(topicName))
            .findFirst();
    }

    private static Subscriber subscribeToTopic(final QueueableTicketRegistryMessageReceiver messageQueueTicketRegistryReceiver,
                                               final PubSubTemplate pubSubTemplate,
                                               final Topic topic,
                                               final Subscription subscription,
                                               final PubSubMessageConverter messageQueueTicketRegistryConverter) {
        val messageConsumer = new GoogleCloudTicketRegistryMessageQueueConsumer(topic, subscription,
            messageQueueTicketRegistryReceiver, messageQueueTicketRegistryConverter);
        return pubSubTemplate.subscribe(subscription.getName(), messageConsumer);
    }

    private static Subscription getOrCreateSubscription(final PubSubAdmin pubSubAdmin,
                                                        final List<Subscription> allSubcriptions,
                                                        final String subscriptionName,
                                                        final Topic topic,
                                                        final Topic deadLetterTopic) {
        return allSubcriptions
            .stream()
            .filter(sub -> sub.getName().contains(subscriptionName))
            .findFirst()
            .orElseGet(() -> {
                val deadLetterPolicy = DeadLetterPolicy
                    .newBuilder()
                    .setDeadLetterTopic(deadLetterTopic.getName())
                    .setMaxDeliveryAttempts(5)
                    .build();
                return pubSubAdmin.createSubscription(Subscription.newBuilder()
                    .setName(subscriptionName)
                    .setTopic(topic.getName())
                    .setDeadLetterPolicy(deadLetterPolicy));
            });
    }
}
