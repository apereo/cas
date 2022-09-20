package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.ticket.registry.JmsTicketRegistry;
import org.apereo.cas.ticket.registry.JmsTicketRegistryQueuePublisher;
import org.apereo.cas.ticket.registry.JmsTicketRegistryQueueReceiver;
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
 * This is {@link JmsTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry, module = "jms")
@AutoConfiguration
public class JmsTicketRegistryConfiguration {
    @ConditionalOnMissingBean(name = "messageQueueTicketRegistryIdentifier")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PublisherIdentifier messageQueueTicketRegistryIdentifier(
        final CasConfigurationProperties casProperties) {
        val bean = new PublisherIdentifier();
        val jms = casProperties.getTicket().getRegistry().getJms();
        if (StringUtils.isNotBlank(jms.getQueueIdentifier())) {
            bean.setId(jms.getQueueIdentifier());
        }
        return bean;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "messageQueueTicketRegistryReceiver")
    public JmsTicketRegistryQueueReceiver messageQueueTicketRegistryReceiver(
        @Qualifier(TicketRegistry.BEAN_NAME)
        final TicketRegistry ticketRegistry,
        @Qualifier("messageQueueTicketRegistryIdentifier")
        final PublisherIdentifier messageQueueTicketRegistryIdentifier) {
        return new JmsTicketRegistryQueueReceiver(ticketRegistry, messageQueueTicketRegistryIdentifier);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MessageConverter jacksonJmsMessageTicketRegistryConverter() {
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
        @Qualifier("jacksonJmsMessageTicketRegistryConverter")
        final MessageConverter jacksonJmsMessageConverter,
        @Qualifier(TicketSerializationManager.BEAN_NAME)
        final TicketSerializationManager ticketSerializationManager,
        @Qualifier("messageQueueTicketRegistryIdentifier")
        final PublisherIdentifier messageQueueTicketRegistryIdentifier,
        final CasConfigurationProperties casProperties) {

        rabbitTemplate.setMessageConverter(jacksonJmsMessageConverter);
        val jms = casProperties.getTicket().getRegistry().getJms();
        val cipher = CoreTicketUtils.newTicketRegistryCipherExecutor(jms.getCrypto(), "jms");
        LOGGER.debug("Configuring JMS ticket registry with identifier [{}]", messageQueueTicketRegistryIdentifier);
        val registry = new JmsTicketRegistry(new JmsTicketRegistryQueuePublisher(rabbitTemplate),
            messageQueueTicketRegistryIdentifier, ticketSerializationManager);
        registry.setCipherExecutor(cipher);
        return registry;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "jmsTicketRegistryQueue")
    public Queue jmsTicketRegistryQueue() {
        return new Queue(JmsTicketRegistry.class.getSimpleName(), false);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "jmsTicketRegistryExchange")
    public TopicExchange jmsTicketRegistryExchange() {
        return new TopicExchange(JmsTicketRegistryQueuePublisher.QUEUE_DESTINATION);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "jmsTicketRegistryBinding")
    public Binding jmsTicketRegistryBinding(
        @Qualifier("jmsTicketRegistryQueue")
        final Queue jmsTicketRegistryQueue,
        @Qualifier("jmsTicketRegistryExchange")
        final TopicExchange jmsTicketRegistryExchange) {
        return BindingBuilder.bind(jmsTicketRegistryQueue).to(jmsTicketRegistryExchange).with("cas.#");
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "jmsTicketRegistryMessageListenerContainer")
    public MessageListenerContainer jmsTicketRegistryMessageListenerContainer(
        @Qualifier("rabbitConnectionFactory")
        final ConnectionFactory connectionFactory,
        @Qualifier("jmsTicketRegistryListenerAdapter")
        final MessageListenerAdapter listenerAdapter) {
        val container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(JmsTicketRegistry.class.getSimpleName());
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "jmsTicketRegistryListenerAdapter")
    public MessageListenerAdapter jmsTicketRegistryListenerAdapter(
        @Qualifier("messageQueueTicketRegistryReceiver")
        final JmsTicketRegistryQueueReceiver messageQueueTicketRegistryReceiver,
        @Qualifier("jacksonJmsMessageTicketRegistryConverter")
        final MessageConverter jacksonJmsMessageTicketRegistryConverter) {
        val adapter = new MessageListenerAdapter(messageQueueTicketRegistryReceiver, "receive");
        adapter.setMessageConverter(jacksonJmsMessageTicketRegistryConverter);
        return adapter;
    }

}
