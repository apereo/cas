package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.AMQPDefaultTicketRegistry;
import org.apereo.cas.ticket.registry.AMQPMessageSerializationHandler;
import org.apereo.cas.ticket.registry.AMQPTicketRegistry;
import org.apereo.cas.ticket.registry.AMQPTicketRegistryQueueReceiver;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.queue.AMQPTicketRegistryQueuePublisher;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
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

import java.nio.charset.StandardCharsets;

/**
 * This is {@link AMQPTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry, module = "amqp")
@AutoConfiguration
public class AMQPTicketRegistryConfiguration {
    @ConditionalOnMissingBean(name = "messageQueueTicketRegistryIdentifier")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PublisherIdentifier messageQueueTicketRegistryIdentifier(
        final CasConfigurationProperties casProperties) {
        val bean = new PublisherIdentifier();
        val amqp = casProperties.getTicket().getRegistry().getAmqp();

        FunctionUtils.doIfNotBlank(amqp.getQueueIdentifier(), __ -> bean.setId(amqp.getQueueIdentifier()));
        return bean;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "messageQueueTicketRegistryReceiver")
    @Lazy(false)
    public AMQPTicketRegistryQueueReceiver messageQueueTicketRegistryReceiver(
        @Qualifier(TicketRegistry.BEAN_NAME)
        final AMQPTicketRegistry ticketRegistry,
        @Qualifier("messageQueueTicketRegistryIdentifier")
        final PublisherIdentifier messageQueueTicketRegistryIdentifier) {
        return new AMQPTicketRegistryQueueReceiver(ticketRegistry, messageQueueTicketRegistryIdentifier);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "messageQueueCipherExecutor")
    public CipherExecutor messageQueueCipherExecutor(final CasConfigurationProperties casProperties) {
        val amqp = casProperties.getTicket().getRegistry().getAmqp();
        return CoreTicketUtils.newTicketRegistryCipherExecutor(amqp.getCrypto(), "amqp");
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MessageConverter messageQueueTicketRegistryConverter(
        @Qualifier("messageQueueCipherExecutor")
        final CipherExecutor messageQueueCipherExecutor) {
        val converter = new SerializerMessageConverter();
        converter.setDefaultCharset(StandardCharsets.UTF_8.name());
        converter.setSerializer(new AMQPMessageSerializationHandler(messageQueueCipherExecutor));
        converter.setDeserializer(new AMQPMessageSerializationHandler(messageQueueCipherExecutor));
        return converter;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AMQPTicketRegistry ticketRegistry(
        @Qualifier(TicketCatalog.BEAN_NAME)
        final TicketCatalog ticketCatalog,
        @Qualifier(TicketSerializationManager.BEAN_NAME)
        final TicketSerializationManager ticketSerializationManager,
        @Qualifier("messageQueueCipherExecutor")
        final CipherExecutor messageQueueCipherExecutor,
        final RabbitTemplate rabbitTemplate,
        @Qualifier("messageQueueTicketRegistryConverter")
        final MessageConverter messageQueueTicketRegistryConverter,
        @Qualifier("messageQueueTicketRegistryIdentifier")
        final PublisherIdentifier messageQueueTicketRegistryIdentifier) {
        rabbitTemplate.setMessageConverter(messageQueueTicketRegistryConverter);
        LOGGER.debug("Configuring AMQP ticket registry with identifier [{}]", messageQueueTicketRegistryIdentifier);
        return new AMQPDefaultTicketRegistry(messageQueueCipherExecutor, ticketSerializationManager, ticketCatalog,
            new AMQPTicketRegistryQueuePublisher(rabbitTemplate), messageQueueTicketRegistryIdentifier);
    }

    @Bean
    @ConditionalOnMissingBean(name = "messageQueueTopicBindings")
    public Declarables messageQueueTopicBindings(
        @Qualifier("messageQueueTicketRegistryIdentifier")
        final PublisherIdentifier messageQueueTicketRegistryIdentifier) {
        val queue = new Queue(messageQueueTicketRegistryIdentifier.getId(), false, false, true);
        val topicExchange = new TopicExchange(AMQPTicketRegistryQueuePublisher.QUEUE_DESTINATION, false, true);
        return new Declarables(queue, topicExchange, BindingBuilder.bind(queue).to(topicExchange).with("#"));
    }

    @Bean
    @ConditionalOnMissingBean(name = "amqpTicketRegistryMessageListenerContainer")
    @Lazy(false)
    public MessageListenerContainer amqpTicketRegistryMessageListenerContainer(
        @Qualifier("messageQueueTicketRegistryIdentifier")
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
        final AMQPTicketRegistryQueueReceiver messageQueueTicketRegistryReceiver,
        @Qualifier("messageQueueTicketRegistryConverter")
        final MessageConverter messageQueueTicketRegistryConverter) {
        val adapter = new MessageListenerAdapter(messageQueueTicketRegistryReceiver, "receive");
        adapter.setMessageConverter(messageQueueTicketRegistryConverter);
        return adapter;
    }

}
