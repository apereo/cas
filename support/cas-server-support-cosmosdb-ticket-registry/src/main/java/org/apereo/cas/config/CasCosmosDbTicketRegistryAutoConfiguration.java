package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.cosmosdb.CosmosDbObjectFactory;
import org.apereo.cas.ticket.CosmosDbTicketRegistry;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.catalog.CasTicketCatalogConfigurationValuesProvider;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasCosmosDbTicketRegistryAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry, module = "cosmosdb")
@AutoConfiguration
public class CasCosmosDbTicketRegistryAutoConfiguration {

    @ConditionalOnMissingBean(name = "cosmosDbTicketCatalogConfigurationValuesProvider")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasTicketCatalogConfigurationValuesProvider cosmodDbTicketCatalogConfigurationValuesProvider() {
        return new CasTicketCatalogConfigurationValuesProvider() {
        };
    }


    @ConditionalOnMissingBean(name = "cosmosDbTicketRegistryObjectFactory")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CosmosDbObjectFactory cosmosDbTicketRegistryObjectFactory(
        @Qualifier(CasSSLContext.BEAN_NAME)
        final CasSSLContext casSslContext,
        final CasConfigurationProperties casProperties) {
        return new CosmosDbObjectFactory(casProperties.getTicket().getRegistry().getCosmosDb(), casSslContext);
    }

    @Bean
    @ConditionalOnMissingBean(name = "cosmosDbTicketRegistry")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TicketRegistry ticketRegistry(
        final CasConfigurationProperties casProperties,
        @Qualifier(TicketSerializationManager.BEAN_NAME)
        final TicketSerializationManager ticketSerializationManager,
        @Qualifier(TicketCatalog.BEAN_NAME)
        final TicketCatalog ticketCatalog,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("cosmosDbTicketRegistryObjectFactory")
        final CosmosDbObjectFactory cosmosDbTicketRegistryObjectFactory) {

        cosmosDbTicketRegistryObjectFactory.createDatabaseIfNecessary();
        val ticketDefinitions = ticketCatalog.findAll();
        val containers = ticketDefinitions
            .stream()
            .map(defn -> {
                LOGGER.debug("Creating CosmosDb container [{}] for [{}]:[{}]",
                    defn.getProperties().getStorageName(), defn.getPrefix(), defn.getApiClass().getSimpleName());
                return cosmosDbTicketRegistryObjectFactory.createContainer(defn.getProperties().getStorageName(),
                    defn.getProperties().getStorageTimeout(), CosmosDbTicketRegistry.PARTITION_KEY_PREFIX);
            })
            .toList();

        val cipher = CoreTicketUtils.newTicketRegistryCipherExecutor(
            casProperties.getTicket().getRegistry().getCosmosDb().getCrypto(), "cosmos-db");
        return new CosmosDbTicketRegistry(cipher, ticketSerializationManager, ticketCatalog, applicationContext, containers);
    }
}
