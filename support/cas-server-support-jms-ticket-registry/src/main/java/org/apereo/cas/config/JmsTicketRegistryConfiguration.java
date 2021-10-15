package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.registry.JmsTicketRegistry;
import org.apereo.cas.ticket.registry.JmsTicketRegistryDefaultPublisher;
import org.apereo.cas.ticket.registry.JmsTicketRegistryReceiver;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
@Configuration(value = "jmsTicketRegistryConfiguration", proxyBeanMethods = false)
public class JmsTicketRegistryConfiguration {
    @ConditionalOnMissingBean(name = "messageQueueTicketRegistryIdentifier")
    @Bean
    @Autowired
    public PublisherIdentifier messageQueueTicketRegistryIdentifier(final CasConfigurationProperties casProperties) {
        val bean = new PublisherIdentifier();
        val jms = casProperties.getTicket().getRegistry().getJms();
        if (StringUtils.isNotBlank(jms.getQueueIdentifier())) {
            bean.setId(jms.getQueueIdentifier());
        }
        return bean;
    }

    @Bean
    public JmsTicketRegistryReceiver messageQueueTicketRegistryReceiver(
        @Qualifier(TicketRegistry.BEAN_NAME)
        final TicketRegistry ticketRegistry,
        final CasConfigurationProperties casProperties,
        @Qualifier("messageQueueTicketRegistryIdentifier")
        final PublisherIdentifier messageQueueTicketRegistryIdentifier) {
        return new JmsTicketRegistryReceiver(ticketRegistry, messageQueueTicketRegistryIdentifier);
    }

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
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
        @Qualifier("jacksonJmsMessageConverter")
        final MessageConverter jacksonJmsMessageConverter) {
        jmsTemplate.setMessageConverter(jacksonJmsMessageConverter);
        val jms = casProperties.getTicket().getRegistry().getJms();
        val cipher = CoreTicketUtils.newTicketRegistryCipherExecutor(jms.getCrypto(), "jms");
        LOGGER.debug("Configuring JMS ticket registry with identifier [{}]", messageQueueTicketRegistryIdentifier);
        return new JmsTicketRegistry(new JmsTicketRegistryDefaultPublisher(jmsTemplate), messageQueueTicketRegistryIdentifier, cipher);
    }

    @ConditionalOnMissingBean(name = "messageQueueTicketRegistryFactory")
    @Bean
    public JmsListenerContainerFactory<?> messageQueueTicketRegistryFactory(
        @Qualifier("jacksonJmsMessageConverter")
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
