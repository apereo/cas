package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.ticket.registry.AMQPTicketRegistry;
import org.apereo.cas.ticket.registry.AMQPTicketRegistryQueuePublisher;
import org.apereo.cas.ticket.registry.AMQPTicketRegistryQueueReceiver;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
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
        if (StringUtils.isNotBlank(amqp.getQueueIdentifier())) {
            bean.setId(amqp.getQueueIdentifier());
        }
        return bean;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "messageQueueTicketRegistryReceiver")
    public AMQPTicketRegistryQueueReceiver messageQueueTicketRegistryReceiver(
        @Qualifier(TicketRegistry.BEAN_NAME)
        final TicketRegistry ticketRegistry,
        @Qualifier("messageQueueTicketRegistryIdentifier")
        final PublisherIdentifier messageQueueTicketRegistryIdentifier) {
        return new AMQPTicketRegistryQueueReceiver(ticketRegistry, messageQueueTicketRegistryIdentifier);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MessageConverter messageQueueTicketRegistryConverter() {
        val mapper = JacksonObjectMapperFactory.builder()
            .defaultTypingEnabled(true)
            .defaultViewInclusion(false)
            .writeDatesAsTimestamps(true)
            .build()
            .toObjectMapper();

        val converter = new Jackson2JsonMessageConverter(mapper);
        converter.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID);
        converter.setDefaultCharset(StandardCharsets.UTF_8.name());
        return converter;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TicketRegistry ticketRegistry(
        final RabbitTemplate rabbitTemplate,
        @Qualifier("messageQueueTicketRegistryConverter")
        final MessageConverter messageQueueTicketRegistryConverter,
        @Qualifier(TicketSerializationManager.BEAN_NAME)
        final TicketSerializationManager ticketSerializationManager,
        @Qualifier("messageQueueTicketRegistryIdentifier")
        final PublisherIdentifier messageQueueTicketRegistryIdentifier,
        final CasConfigurationProperties casProperties) {

        rabbitTemplate.setMessageConverter(messageQueueTicketRegistryConverter);
        val amqp = casProperties.getTicket().getRegistry().getAmqp();
        val cipher = CoreTicketUtils.newTicketRegistryCipherExecutor(amqp.getCrypto(), "amqp");
        LOGGER.debug("Configuring AMQP ticket registry with identifier [{}]", messageQueueTicketRegistryIdentifier);
        val registry = new AMQPTicketRegistry(new AMQPTicketRegistryQueuePublisher(rabbitTemplate),
            messageQueueTicketRegistryIdentifier, ticketSerializationManager);
        registry.setCipherExecutor(cipher);
        return registry;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "amqpTicketRegistryQueue")
    public Queue amqpTicketRegistryQueue() {
        return new Queue(AMQPTicketRegistry.class.getSimpleName(), false);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "amqpTicketRegistryExchange")
    public TopicExchange amqpTicketRegistryExchange() {
        return new TopicExchange(AMQPTicketRegistryQueuePublisher.QUEUE_DESTINATION);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "amqpTicketRegistryBinding")
    public Binding amqpTicketRegistryBinding(
        @Qualifier("amqpTicketRegistryQueue")
        final Queue amqpTicketRegistryQueue,
        @Qualifier("amqpTicketRegistryExchange")
        final TopicExchange amqpTicketRegistryExchange) {
        return BindingBuilder.bind(amqpTicketRegistryQueue).to(amqpTicketRegistryExchange).with("cas.#");
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "amqpTicketRegistryMessageListenerContainer")
    public MessageListenerContainer amqpTicketRegistryMessageListenerContainer(
        @Qualifier("rabbitConnectionFactory")
        final ConnectionFactory connectionFactory,
        @Qualifier("amqpTicketRegistryListenerAdapter")
        final MessageListenerAdapter listenerAdapter) {
        val container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(AMQPTicketRegistry.class.getSimpleName());
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "amqpTicketRegistryListenerAdapter")
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
