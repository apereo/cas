package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.MongoDbTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.MongoDbTicketRegistryFacilitator;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * This is {@link MongoDbTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "mongoTicketRegistryConfiguration", proxyBeanMethods = false)
public class MongoDbTicketRegistryConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public TicketRegistry ticketRegistry(
        @Qualifier("ticketCatalog")
        final TicketCatalog ticketCatalog, final CasConfigurationProperties casProperties,
        @Qualifier("mongoDbTicketRegistryTemplate")
        final MongoTemplate mongoDbTicketRegistryTemplate,
        @Qualifier("ticketSerializationManager")
        final TicketSerializationManager ticketSerializationManager) {
        val mongo = casProperties.getTicket().getRegistry().getMongo();
        val registry = new MongoDbTicketRegistry(ticketCatalog, mongoDbTicketRegistryTemplate, ticketSerializationManager);
        registry.setCipherExecutor(CoreTicketUtils.newTicketRegistryCipherExecutor(mongo.getCrypto(), "mongo"));
        new MongoDbTicketRegistryFacilitator(ticketCatalog, mongoDbTicketRegistryTemplate,
            mongo.isDropCollection(), mongo.isUpdateIndexes(), mongo.isDropIndexes()).createTicketCollections();
        return registry;
    }

    @ConditionalOnMissingBean(name = "mongoDbTicketRegistryTemplate")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MongoTemplate mongoDbTicketRegistryTemplate(
        final CasConfigurationProperties casProperties,
        @Qualifier("casSslContext")
        final CasSSLContext casSslContext) {
        val factory = new MongoDbConnectionFactory(casSslContext.getSslContext());
        val mongo = casProperties.getTicket().getRegistry().getMongo();
        return factory.buildMongoTemplate(mongo);
    }
}
