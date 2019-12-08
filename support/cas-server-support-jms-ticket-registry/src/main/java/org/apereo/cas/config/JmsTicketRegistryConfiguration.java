package org.apereo.cas.config;

import org.apereo.cas.JmsQueueIdentifier;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.registry.JmsTicketRegistry;
import org.apereo.cas.ticket.registry.JmsTicketRegistryDefaultPublisher;
import org.apereo.cas.ticket.registry.JmsTicketRegistryPublisher;
import org.apereo.cas.ticket.registry.JmsTicketRegistryReceiver;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CoreTicketUtils;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@Configuration("jmsTicketRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableJms
@Slf4j
public class JmsTicketRegistryConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ObjectProvider<JmsTemplate> jmsTemplate;

    @Autowired
    private ObjectProvider<DefaultJmsListenerContainerFactoryConfigurer> jmsListenerContainerFactoryConfigurer;

    @Autowired
    private ObjectProvider<ConnectionFactory> jmsConnectionFactory;

    @ConditionalOnMissingBean(name = "messageQueueTicketRegistryIdentifier")
    @Bean
    public JmsQueueIdentifier messageQueueTicketRegistryIdentifier() {
        val bean = new JmsQueueIdentifier();
        val jms = casProperties.getTicket().getRegistry().getJms();
        if (StringUtils.isNotBlank(jms.getQueueIdentifier())) {
            bean.setId(jms.getQueueIdentifier());
        }
        return bean;
    }

    @Bean
    public JmsTicketRegistryReceiver messageQueueTicketRegistryReceiver() {
        return new JmsTicketRegistryReceiver(
            getJmsTicketRegistryWithPublisher(JmsTicketRegistryPublisher.noOp()),
            messageQueueTicketRegistryIdentifier());
    }

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        val converter = new MappingJackson2MessageConverter();

        val mapper = new ObjectMapper().findAndRegisterModules()
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false)
            .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.activateDefaultTyping(mapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        converter.setObjectMapper(mapper);
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("@class");
        return converter;
    }

    @Bean
    @RefreshScope
    public TicketRegistry ticketRegistry() {
        val template = this.jmsTemplate.getObject();
        template.setMessageConverter(jacksonJmsMessageConverter());
        return getJmsTicketRegistryWithPublisher(new JmsTicketRegistryDefaultPublisher(template));
    }

    @ConditionalOnMissingBean(name = "messageQueueTicketRegistryFactory")
    @Bean
    public JmsListenerContainerFactory<?> messageQueueTicketRegistryFactory() {
        val factory = new DefaultJmsListenerContainerFactory();
        factory.setMessageConverter(jacksonJmsMessageConverter());
        jmsListenerContainerFactoryConfigurer.getObject().configure(factory, jmsConnectionFactory.getObject());
        return factory;
    }

    private TicketRegistry getJmsTicketRegistryWithPublisher(final JmsTicketRegistryPublisher publisher) {
        val jms = casProperties.getTicket().getRegistry().getJms();
        val cipher = CoreTicketUtils.newTicketRegistryCipherExecutor(jms.getCrypto(), "jms");
        val messageQueueTicketRegistryIdentifier = messageQueueTicketRegistryIdentifier();
        LOGGER.debug("Configuring JMS ticket registry with identifier [{}]", messageQueueTicketRegistryIdentifier);
        return new JmsTicketRegistry(publisher, messageQueueTicketRegistryIdentifier, cipher);
    }
}
