package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.MongoDbTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.MongoDbTicketRegistryFacilitator;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.mongodb.core.MongoOperations;

/**
 * This is {@link MongoDbTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry, module = "mongo")
@Configuration(value = "MongoDbTicketRegistryConfiguration", proxyBeanMethods = false)
class MongoDbTicketRegistryConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public TicketRegistry ticketRegistry(
        @Qualifier(TicketCatalog.BEAN_NAME)
        final TicketCatalog ticketCatalog,
        final CasConfigurationProperties casProperties,
        @Qualifier("mongoDbTicketRegistryTemplate")
        final MongoOperations mongoDbTicketRegistryTemplate,
        @Qualifier(TicketSerializationManager.BEAN_NAME)
        final TicketSerializationManager ticketSerializationManager,
        final ConfigurableApplicationContext applicationContext) {

        val mongo = casProperties.getTicket().getRegistry().getMongo();
        new MongoDbTicketRegistryFacilitator(ticketCatalog, mongoDbTicketRegistryTemplate, mongo).createTicketCollections();

        val cipher = CoreTicketUtils.newTicketRegistryCipherExecutor(mongo.getCrypto(), "mongo");
        return new MongoDbTicketRegistry(cipher, ticketSerializationManager, ticketCatalog, applicationContext, mongoDbTicketRegistryTemplate);
    }

    @ConditionalOnMissingBean(name = "mongoDbTicketRegistryTemplate")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MongoOperations mongoDbTicketRegistryTemplate(
        final CasConfigurationProperties casProperties,
        @Qualifier(CasSSLContext.BEAN_NAME)
        final CasSSLContext casSslContext) {
        val factory = new MongoDbConnectionFactory(casSslContext.getSslContext());
        val mongo = casProperties.getTicket().getRegistry().getMongo();
        return factory.buildMongoTemplate(mongo);
    }
}
