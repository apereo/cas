package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.dynamodb.AmazonDynamoDbClientFactory;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.catalog.CasTicketCatalogConfigurationValuesProvider;
import org.apereo.cas.ticket.registry.DynamoDbTicketRegistry;
import org.apereo.cas.ticket.registry.DynamoDbTicketRegistryFacilitator;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import java.util.function.Function;

/**
 * This is {@link CasDynamoDbTicketRegistryAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry, module = "dynamodb")
@AutoConfiguration
public class CasDynamoDbTicketRegistryAutoConfiguration {

    @Configuration(value = "DynamoDbTicketRegistryBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class DynamoDbTicketRegistryBaseConfiguration {
        @ConditionalOnMissingBean(name = "dynamoDbTicketCatalogConfigurationValuesProvider")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasTicketCatalogConfigurationValuesProvider dynamoDbTicketCatalogConfigurationValuesProvider() {
            return new CasTicketCatalogConfigurationValuesProvider() {
                @Override
                public Function<CasConfigurationProperties, String> getServiceTicketStorageName() {
                    return p -> p.getTicket().getRegistry().getDynamoDb().getServiceTicketsTableName();
                }

                @Override
                public Function<CasConfigurationProperties, String> getProxyTicketStorageName() {
                    return p -> p.getTicket().getRegistry().getDynamoDb().getProxyTicketsTableName();
                }

                @Override
                public Function<CasConfigurationProperties, String> getTicketGrantingTicketStorageName() {
                    return p -> p.getTicket().getRegistry().getDynamoDb().getTicketGrantingTicketsTableName();
                }

                @Override
                public Function<CasConfigurationProperties, String> getProxyGrantingTicketStorageName() {
                    return p -> p.getTicket().getRegistry().getDynamoDb().getProxyGrantingTicketsTableName();
                }

                @Override
                public Function<CasConfigurationProperties, String> getTransientSessionStorageName() {
                    return p -> p.getTicket().getRegistry().getDynamoDb().getTransientSessionTicketsTableName();
                }
            };
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public TicketRegistry ticketRegistry(
            @Qualifier(TicketCatalog.BEAN_NAME)
            final TicketCatalog ticketCatalog,
            @Qualifier(TicketSerializationManager.BEAN_NAME)
            final TicketSerializationManager ticketSerializationManager,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("dynamoDbTicketRegistryFacilitator")
            final DynamoDbTicketRegistryFacilitator dynamoDbTicketRegistryFacilitator,
            final CasConfigurationProperties casProperties) {
            val db = casProperties.getTicket().getRegistry().getDynamoDb();
            val crypto = db.getCrypto();
            val cipherExecutor = CoreTicketUtils.newTicketRegistryCipherExecutor(crypto, "dynamo-db");
            return new DynamoDbTicketRegistry(cipherExecutor, ticketSerializationManager, ticketCatalog, applicationContext,
                dynamoDbTicketRegistryFacilitator);
        }
    }

    @Configuration(value = "DynamoDbTicketRegistryHelperConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class DynamoDbTicketRegistryHelperConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public DynamoDbTicketRegistryFacilitator dynamoDbTicketRegistryFacilitator(
            @Qualifier("amazonDynamoDbTicketRegistryClient")
            final DynamoDbClient amazonDynamoDbTicketRegistryClient,
            final CasConfigurationProperties casProperties,
            @Qualifier(TicketCatalog.BEAN_NAME)
            final TicketCatalog ticketCatalog) {
            val db = casProperties.getTicket().getRegistry().getDynamoDb();
            val facilitator = new DynamoDbTicketRegistryFacilitator(ticketCatalog, db, amazonDynamoDbTicketRegistryClient);
            if (!db.isPreventTableCreationOnStartup()) {
                facilitator.createTicketTables(db.isDropTablesOnStartup());
            }
            return facilitator;
        }

    }

    @Configuration(value = "DynamoDbTicketRegistryClientConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class DynamoDbTicketRegistryClientConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "amazonDynamoDbTicketRegistryClient")
        public DynamoDbClient amazonDynamoDbTicketRegistryClient(final CasConfigurationProperties casProperties) {
            val dynamoDbProperties = casProperties.getTicket().getRegistry().getDynamoDb();
            val factory = new AmazonDynamoDbClientFactory();
            return factory.createAmazonDynamoDb(dynamoDbProperties);
        }
    }

}
