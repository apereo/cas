package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.kafka.KafkaObjectFactory;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.catalog.CasTicketCatalogConfigurationValuesProvider;
import org.apereo.cas.ticket.registry.publisher.KafkaTicketRegistryPublisher;
import org.apereo.cas.ticket.registry.pubsub.commands.BaseMessageQueueCommand;
import org.apereo.cas.ticket.registry.pubsub.queue.QueueableTicketRegistryMessagePublisher;
import org.apereo.cas.ticket.registry.pubsub.queue.QueueableTicketRegistryMessageReceiver;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.MethodKafkaListenerEndpoint;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaAdminOperations;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import tools.jackson.databind.json.JsonMapper;
import java.util.Map;
import java.util.UUID;

/**
 * This is {@link CasKafkaTicketRegistryAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry, module = "kafka")
@AutoConfiguration
@Lazy(false)
@Slf4j
@EnableKafka
public class CasKafkaTicketRegistryAutoConfiguration {
    private static final JsonMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).minimal(true).build().toJsonMapper();

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "kafkaTicketRegistryTemplate")
    public KafkaOperations<@NonNull String, @NonNull BaseMessageQueueCommand> kafkaTicketRegistryTemplate(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val kafka = casProperties.getTicket().getRegistry().getKafka();
        val factory = new KafkaObjectFactory<String, BaseMessageQueueCommand>(kafka.getBootstrapAddress());
        return factory.getKafkaTemplate(new StringSerializer(), new JacksonJsonSerializer<>(MAPPER));
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "kafkaTicketRegistryAdminOperations")
    public KafkaAdminOperations kafkaTicketRegistryAdminOperations(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val kafka = casProperties.getTicket().getRegistry().getKafka();
        val factory = new KafkaObjectFactory<String, BaseMessageQueueCommand>(kafka.getBootstrapAddress());
        val kafkaAdmin = factory.getKafkaAdmin();
        kafkaAdmin.setAutoCreate(kafka.isAutoCreateTopics());
        return kafkaAdmin;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "kafkaTicketRegistryPublisher")
    public QueueableTicketRegistryMessagePublisher messageQueueTicketRegistryPublisher(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(TicketCatalog.BEAN_NAME)
        final TicketCatalog ticketCatalog,
        @Qualifier(PublisherIdentifier.DEFAULT_BEAN_NAME)
        final PublisherIdentifier messageQueueTicketRegistryIdentifier,
        @Qualifier("kafkaTicketRegistryTemplate")
        final KafkaOperations<@NonNull String, @NonNull BaseMessageQueueCommand> kafkaTicketRegistryTemplate) {
        LOGGER.debug("Configuring Kafka ticket registry with identifier [{}]", messageQueueTicketRegistryIdentifier);
        return new KafkaTicketRegistryPublisher(applicationContext, ticketCatalog, kafkaTicketRegistryTemplate);
    }

    @ConditionalOnMissingBean(name = "kafkaTicketCatalogConfigurationValuesProvider")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasTicketCatalogConfigurationValuesProvider kafkaTicketCatalogConfigurationValuesProvider() {
        return new CasTicketCatalogConfigurationValuesProvider() {
        };
    }

    @Bean
    @ConditionalOnMissingBean(name = "kafkaTicketRegistryConsumerFactory")
    public ConsumerFactory<@NonNull String, @NonNull BaseMessageQueueCommand> kafkaTicketRegistryConsumerFactory(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val kafka = casProperties.getTicket().getRegistry().getKafka();
        val config = Map.<String, Object>of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapAddress(),
            ConsumerConfig.GROUP_ID_CONFIG, kafka.getGroupId()
        );
        val factory = new DefaultKafkaConsumerFactory<@NonNull String, @NonNull BaseMessageQueueCommand>(config);
        factory.setKeyDeserializer(new StringDeserializer());

        val valueDeserializer = new JacksonJsonDeserializer<@NonNull BaseMessageQueueCommand>(MAPPER);
        valueDeserializer.addTrustedPackages(Ticket.class.getPackageName());
        valueDeserializer.addTrustedPackages(BaseMessageQueueCommand.class.getPackageName());
        valueDeserializer.addTrustedPackages(CentralAuthenticationService.NAMESPACE);
        factory.setValueDeserializer(valueDeserializer);
        
        factory.setApplicationContext(applicationContext);
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean(name = "kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<@NonNull String, @NonNull BaseMessageQueueCommand> kafkaListenerContainerFactory(
        @Qualifier("kafkaTicketRegistryConsumerFactory")
        final ConsumerFactory<@NonNull String, @NonNull BaseMessageQueueCommand> kafkaTicketRegistryConsumerFactory) {
        val factory = new ConcurrentKafkaListenerContainerFactory<@NonNull String, @NonNull BaseMessageQueueCommand>();
        factory.setConsumerFactory(kafkaTicketRegistryConsumerFactory);
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean(name = "kafkaTicketRegistryMessageListenerContainer")
    public ConcurrentMessageListenerContainer<@NonNull String, @NonNull BaseMessageQueueCommand> kafkaTicketRegistryMessageListenerContainer(
        @Qualifier("messageHandlerMethodFactory")
        final MessageHandlerMethodFactory messageHandlerMethodFactory,
        @Qualifier("messageQueueTicketRegistryReceiver")
        final QueueableTicketRegistryMessageReceiver messageQueueTicketRegistryReceiver,
        final CasConfigurationProperties casProperties,
        @Qualifier("kafkaTicketRegistryConsumerFactory")
        final ConsumerFactory<@NonNull String, @NonNull BaseMessageQueueCommand> kafkaTicketRegistryConsumerFactory,
        @Qualifier(TicketCatalog.BEAN_NAME)
        final TicketCatalog ticketCatalog) throws Exception {
        
        val kafka = casProperties.getTicket().getRegistry().getKafka();

        val factory = new ConcurrentKafkaListenerContainerFactory<@NonNull String, @NonNull BaseMessageQueueCommand>();
        factory.setConsumerFactory(kafkaTicketRegistryConsumerFactory);
        factory.setConcurrency(kafka.getConcurrency());

        val containerProperties = factory.getContainerProperties();
        containerProperties.setGroupId(kafka.getGroupId());
        
        val topics = ticketCatalog.findAll()
            .stream()
            .map(definition -> definition.getProperties().getStorageName())
            .toList()
            .toArray(String[]::new);

        val kafkaListenerEndpoint = new MethodKafkaListenerEndpoint<>();
        kafkaListenerEndpoint.setTopics(topics);
        kafkaListenerEndpoint.setGroupId(containerProperties.getGroupId());
        kafkaListenerEndpoint.setId(UUID.randomUUID().toString());

        kafkaListenerEndpoint.setBean(messageQueueTicketRegistryReceiver);
        val listenerMethod = messageQueueTicketRegistryReceiver.getClass().getMethod("receive", BaseMessageQueueCommand.class);
        kafkaListenerEndpoint.setMethod(listenerMethod);

        kafkaListenerEndpoint.setMessageHandlerMethodFactory(messageHandlerMethodFactory);
        return factory.createListenerContainer(kafkaListenerEndpoint);
    }

    @Bean
    @ConditionalOnMissingBean(name = "kafkaMessageHandlerMethodFactory")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MessageHandlerMethodFactory messageHandlerMethodFactory(
        final ConfigurableApplicationContext applicationContext) {
        val factory = new DefaultMessageHandlerMethodFactory();
        factory.setConversionService(applicationContext.getEnvironment().getConversionService());
        return factory;
    }
}

