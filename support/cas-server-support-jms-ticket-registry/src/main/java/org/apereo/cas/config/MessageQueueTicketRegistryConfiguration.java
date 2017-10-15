package org.apereo.cas.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apereo.cas.StringBean;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.registry.MessageQueueTicketRegistry;
import org.apereo.cas.ticket.registry.MessageQueueTicketRegistryReceiver;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
 * This is {@link MessageQueueTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("messageQueueTicketRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableJms
public class MessageQueueTicketRegistryConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Bean
    public StringBean messageQueueTicketRegistryIdentifier() {
        return new StringBean();
    }

    @Bean
    public MessageQueueTicketRegistryReceiver messageQueueTicketRegistryReceiver() {
        return new MessageQueueTicketRegistryReceiver(ticketRegistry(), messageQueueTicketRegistryIdentifier());
    }

    @Bean
    public TicketRegistry ticketRegistry() {
        return new MessageQueueTicketRegistry(this.jmsTemplate, messageQueueTicketRegistryIdentifier());
    }

    @Autowired
    @Bean
    public JmsListenerContainerFactory<?> messageQueueTicketRegistryFactory(final ConnectionFactory connectionFactory,
                                                                            final DefaultJmsListenerContainerFactoryConfigurer configurer) {
        final DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        return factory;
    }

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        final MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");

        new AbstractJacksonBackedStringSerializer<Object>() {
            private static final long serialVersionUID = 1466569521275630254L;

            @Override
            protected Class getTypeToSerialize() {
                return Object.class;
            }

            @Override
            protected ObjectMapper initializeObjectMapper() {
                final ObjectMapper mapper = super.initializeObjectMapper();
                converter.setObjectMapper(mapper);
                return mapper;
            }
        };

        return converter;
    }
}
