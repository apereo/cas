package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.ha.ClusterTopologyManager;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.catalog.CasTicketCatalogConfigurationValuesProvider;
import org.apereo.cas.ticket.registry.PulsarClusterTopologyManager;
import org.apereo.cas.ticket.registry.PulsarTicketRegistryMessageListener;
import org.apereo.cas.ticket.registry.publisher.PulsarTicketRegistryPublisher;
import org.apereo.cas.ticket.registry.pubsub.queue.QueueableTicketRegistryMessagePublisher;
import org.apereo.cas.ticket.registry.pubsub.queue.QueueableTicketRegistryMessageReceiver;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.pulsar.common.schema.SchemaType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.pulsar.annotation.EnablePulsar;
import org.springframework.pulsar.annotation.PulsarListenerConfigurer;
import org.springframework.pulsar.config.ConcurrentPulsarListenerContainerFactory;
import org.springframework.pulsar.config.MethodPulsarListenerEndpoint;
import org.springframework.pulsar.core.PulsarAdministration;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.pulsar.listener.AckMode;
import org.springframework.util.ReflectionUtils;

/**
 * This is {@link CasPulsarTicketRegistryAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry, module = "pulsar")
@AutoConfiguration
@Lazy(false)
@Slf4j
@EnablePulsar
public class CasPulsarTicketRegistryAutoConfiguration {
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "pulsarTicketRegistryPublisher")
    public QueueableTicketRegistryMessagePublisher messageQueueTicketRegistryPublisher(
        @Qualifier("pulsarTemplate")
        final PulsarTemplate pulsarTemplate,
        @Qualifier("pulsarAdministration")
        final PulsarAdministration pulsarAdministration,
        @Qualifier(TicketCatalog.BEAN_NAME)
        final TicketCatalog ticketCatalog,
        @Qualifier(PublisherIdentifier.DEFAULT_BEAN_NAME)
        final PublisherIdentifier messageQueueTicketRegistryIdentifier) {
        LOGGER.debug("Configuring Pulsar ticket registry with identifier [{}]", messageQueueTicketRegistryIdentifier);
        return new PulsarTicketRegistryPublisher(ticketCatalog, pulsarTemplate);
    }

    @ConditionalOnMissingBean(name = "pulsarTicketCatalogConfigurationValuesProvider")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasTicketCatalogConfigurationValuesProvider pulsarTicketCatalogConfigurationValuesProvider() {
        return new CasTicketCatalogConfigurationValuesProvider() {
        };
    }

    @Bean
    public PulsarListenerConfigurer ticketRegistryPulsarListenerConfigurer(
        @Qualifier("messageHandlerMethodFactory")
        final MessageHandlerMethodFactory messageHandlerMethodFactory,
        @Qualifier("pulsarListenerContainerFactory")
        final ConcurrentPulsarListenerContainerFactory<?> pulsarListenerContainerFactory,
        @Qualifier("messageQueueTicketRegistryReceiver")
        final QueueableTicketRegistryMessageReceiver messageQueueTicketRegistryReceiver,
        final CasConfigurationProperties casProperties,
        @Qualifier(TicketCatalog.BEAN_NAME)
        final TicketCatalog ticketCatalog,
        @Qualifier(PublisherIdentifier.DEFAULT_BEAN_NAME)
        final PublisherIdentifier messageQueueTicketRegistryIdentifier) {
        return registrar -> {
            val pulsarListenerEndpoint = new MethodPulsarListenerEndpoint<>();
            val topics = ticketCatalog.findAll()
                .stream()
                .map(definition -> definition.getProperties().getStorageName())
                .toList()
                .toArray(String[]::new);
            pulsarListenerEndpoint.setTopics(topics);
            pulsarListenerEndpoint.setId(UUID.randomUUID().toString());

            val pulsarProperties = casProperties.getTicket().getRegistry().getPulsar();
            pulsarListenerEndpoint.setAckMode(AckMode.RECORD);
            pulsarListenerEndpoint.setConcurrency(pulsarProperties.getConcurrency());
            pulsarListenerEndpoint.setSubscriptionName(subscriptionNameFor(pulsarProperties.getSubscriptionName(), messageQueueTicketRegistryIdentifier));
            pulsarListenerEndpoint.setSchemaType(SchemaType.STRING);
            pulsarListenerEndpoint.setBean(new PulsarTicketRegistryMessageListener(messageQueueTicketRegistryReceiver));
            val listenerMethod = ReflectionUtils.findMethod(PulsarTicketRegistryMessageListener.class, "receive", String.class);
            pulsarListenerEndpoint.setMethod(Objects.requireNonNull(listenerMethod));
            pulsarListenerEndpoint.setMessageHandlerMethodFactory(messageHandlerMethodFactory);
            registrar.registerEndpoint(pulsarListenerEndpoint, pulsarListenerContainerFactory);
        };
    }

    @Bean
    @ConditionalOnMissingBean(name = "pulsarMessageHandlerMethodFactory")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MessageHandlerMethodFactory messageHandlerMethodFactory(
        final ConfigurableApplicationContext applicationContext) {
        val factory = new DefaultMessageHandlerMethodFactory();
        factory.setConversionService(applicationContext.getEnvironment().getConversionService());
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean(name = "ticketRegistryPulsarClusterTopologyManager")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ClusterTopologyManager ticketRegistryPulsarClusterTopologyManager(
        @Qualifier("pulsarAdministration")
        final PulsarAdministration pulsarAdministration) {
        return new PulsarClusterTopologyManager(pulsarAdministration);
    }

    /**
     * The subscription of this node. Every node keeps its own copy of the registry and must see every change, so each
     * subscribes under a name of its own: nodes that share an exclusive subscription cannot all attach to it, and nodes that
     * share a shared subscription split the messages between them, so each change reaches one node rather than all of them.
     * The node is told apart by its publisher identifier, as the Kafka registry names its consumer group.
     *
     * @param subscriptionName the configured subscription name, used as a prefix
     * @param identifier       the identifier of this node
     * @return the subscription name of this node
     */
    public static String subscriptionNameFor(final String subscriptionName, final PublisherIdentifier identifier) {
        return "%s-%s".formatted(subscriptionName, identifier.getId());
    }
}
