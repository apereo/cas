package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.ticket.registry.pubsub.MessageQueueMessageSerializationHandler;
import org.apereo.cas.ticket.registry.pubsub.queue.QueueableTicketRegistryMessagePublisher;
import org.apereo.cas.ticket.registry.pubsub.queue.QueueableTicketRegistryMessageReceiver;
import org.apereo.cas.ticket.registry.queue.AMQPTicketRegistryQueuePublisher;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SerializerMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasAMQPTicketRegistryAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry, module = "amqp")
@AutoConfiguration
public class CasAMQPTicketRegistryAutoConfiguration {
    
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MessageConverter messageQueueTicketRegistryConverter(
        @Qualifier(CipherExecutor.BEAN_NAME_TICKET_REGISTRY_CIPHER_EXECUTOR)
        final CipherExecutor defaultTicketRegistryCipherExecutor) {
        val converter = new SerializerMessageConverter();
        converter.setDefaultCharset(StandardCharsets.UTF_8.name());
        converter.setSerializer(new MessageQueueMessageSerializationHandler(defaultTicketRegistryCipherExecutor));
        converter.setDeserializer(new MessageQueueMessageSerializationHandler(defaultTicketRegistryCipherExecutor));
        return converter;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public QueueableTicketRegistryMessagePublisher messageQueueTicketRegistryPublisher(
        @Qualifier(PublisherIdentifier.DEFAULT_BEAN_NAME)
        final PublisherIdentifier messageQueueTicketRegistryIdentifier,
        @Qualifier("messageQueueTicketRegistryConverter")
        final MessageConverter messageQueueTicketRegistryConverter,
        final RabbitTemplate rabbitTemplate) {
        rabbitTemplate.setMessageConverter(messageQueueTicketRegistryConverter);
        LOGGER.debug("Configuring AMQP ticket registry with identifier [{}]", messageQueueTicketRegistryIdentifier);
        return new AMQPTicketRegistryQueuePublisher(rabbitTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(name = "messageQueueTopicBindings")
    public Declarables messageQueueTopicBindings(
        @Qualifier(PublisherIdentifier.DEFAULT_BEAN_NAME)
        final PublisherIdentifier messageQueueTicketRegistryIdentifier) {
        val queue = new Queue(messageQueueTicketRegistryIdentifier.getId(), true, false, true);
        val topicExchange = new TopicExchange(AMQPTicketRegistryQueuePublisher.QUEUE_DESTINATION, true, true);
        return new Declarables(queue, topicExchange, BindingBuilder.bind(queue).to(topicExchange).with("#"));
    }

    @Bean
    @ConditionalOnMissingBean(name = "amqpTicketRegistryMessageListenerContainer")
    @Lazy(false)
    public MessageListenerContainer amqpTicketRegistryMessageListenerContainer(
        @Qualifier(PublisherIdentifier.DEFAULT_BEAN_NAME)
        final PublisherIdentifier messageQueueTicketRegistryIdentifier,
        @Qualifier("rabbitConnectionFactory")
        final ConnectionFactory connectionFactory,
        @Qualifier("amqpTicketRegistryListenerAdapter")
        final MessageListenerAdapter listenerAdapter) {
        val container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(messageQueueTicketRegistryIdentifier.getId());
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "amqpTicketRegistryListenerAdapter")
    @Lazy(false)
    public MessageListenerAdapter amqpTicketRegistryListenerAdapter(
        @Qualifier("messageQueueTicketRegistryReceiver")
        final QueueableTicketRegistryMessageReceiver messageQueueTicketRegistryReceiver,
        @Qualifier("messageQueueTicketRegistryConverter")
        final MessageConverter messageQueueTicketRegistryConverter) {
        val adapter = new MessageListenerAdapter(messageQueueTicketRegistryReceiver, "receive");
        adapter.setMessageConverter(messageQueueTicketRegistryConverter);
        return adapter;
    }

}
