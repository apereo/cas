package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.registry.JmsTicketRegistry;
import org.apereo.cas.ticket.registry.JmsTicketRegistryQueuePublisher;
import org.apereo.cas.ticket.registry.JmsTicketRegistryQueueReceiver;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import javax.jms.ConnectionFactory;

/**
 * This is {@link JmsTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableJms
@Slf4j
@Configuration(value = "JmsTicketRegistryConfiguration", proxyBeanMethods = false)
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
    public MessageConverter jacksonJmsMessageTicketRegistryConverter() {
        val converter = new MappingJackson2MessageConverter();
        val mapper = JacksonObjectMapperFactory.builder()
            .defaultTypingEnabled(true).defaultViewInclusion(false)
            .writeDatesAsTimestamps(true).build().toObjectMapper();
        converter.setObjectMapper(mapper);
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("@class");
        return converter;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TicketRegistry ticketRegistry(
        @Qualifier("messageQueueTicketRegistryIdentifier")
        final PublisherIdentifier messageQueueTicketRegistryIdentifier,
        final CasConfigurationProperties casProperties,
        final JmsTemplate jmsTemplate,
        @Qualifier("jacksonJmsMessageTicketRegistryConverter")
        final MessageConverter jacksonJmsMessageConverter) {
        jmsTemplate.setMessageConverter(jacksonJmsMessageConverter);
        val jms = casProperties.getTicket().getRegistry().getJms();
        val cipher = CoreTicketUtils.newTicketRegistryCipherExecutor(jms.getCrypto(), "jms");
        LOGGER.debug("Configuring JMS ticket registry with identifier [{}]", messageQueueTicketRegistryIdentifier);
        val registry = new JmsTicketRegistry(new JmsTicketRegistryQueuePublisher(jmsTemplate), messageQueueTicketRegistryIdentifier);
        registry.setCipherExecutor(cipher);
        return registry;
    }

    @ConditionalOnMissingBean(name = "messageQueueTicketRegistryFactory")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public JmsListenerContainerFactory<?> messageQueueTicketRegistryFactory(
        @Qualifier("jacksonJmsMessageTicketRegistryConverter")
        final MessageConverter jacksonJmsMessageConverter,
        @Qualifier("jmsListenerContainerFactoryConfigurer")
        final DefaultJmsListenerContainerFactoryConfigurer jmsListenerContainerFactoryConfigurer,
        @Qualifier("jmsConnectionFactory")
        final ConnectionFactory jmsConnectionFactory) {
        val factory = new DefaultJmsListenerContainerFactory();
        factory.setMessageConverter(jacksonJmsMessageConverter);
        jmsListenerContainerFactoryConfigurer.configure(factory, jmsConnectionFactory);
        return factory;
    }
}
