package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.dynamodb.AmazonDynamoDbClientFactory;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.DynamoDbTicketRegistry;
import org.apereo.cas.ticket.registry.DynamoDbTicketRegistryFacilitator;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CoreTicketUtils;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * This is {@link DynamoDbTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "dynamoDbTicketRegistryConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DynamoDbTicketRegistryConfiguration {


    @Configuration(value = "DynamoDbTicketRegistryBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class DynamoDbTicketRegistryBaseConfiguration {
        @Autowired
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public TicketRegistry ticketRegistry(
            @Qualifier("dynamoDbTicketRegistryFacilitator")
            final DynamoDbTicketRegistryFacilitator dynamoDbTicketRegistryFacilitator,
            final CasConfigurationProperties casProperties) {
            val db = casProperties.getTicket().getRegistry().getDynamoDb();
            val crypto = db.getCrypto();
            return new DynamoDbTicketRegistry(CoreTicketUtils.newTicketRegistryCipherExecutor(crypto,
                "dynamo-db"), dynamoDbTicketRegistryFacilitator);
        }
    }

    @Configuration(value = "DynamoDbTicketRegistryHelperConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class DynamoDbTicketRegistryHelperConfiguration {
        @Autowired
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public DynamoDbTicketRegistryFacilitator dynamoDbTicketRegistryFacilitator(
            @Qualifier("amazonDynamoDbTicketRegistryClient")
            final DynamoDbClient amazonDynamoDbTicketRegistryClient,
            final CasConfigurationProperties casProperties,
            @Qualifier("ticketCatalog")
            final TicketCatalog ticketCatalog) {
            val db = casProperties.getTicket().getRegistry().getDynamoDb();
            val f = new DynamoDbTicketRegistryFacilitator(ticketCatalog, db, amazonDynamoDbTicketRegistryClient);
            if (!db.isPreventTableCreationOnStartup()) {
                f.createTicketTables(db.isDropTablesOnStartup());
            }
            return f;
        }
        
    }

    @Configuration(value = "DynamoDbTicketRegistryClientConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class DynamoDbTicketRegistryClientConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        @ConditionalOnMissingBean(name = "amazonDynamoDbTicketRegistryClient")
        public DynamoDbClient amazonDynamoDbTicketRegistryClient(final CasConfigurationProperties casProperties) {
            val dynamoDbProperties = casProperties.getTicket().getRegistry().getDynamoDb();
            val factory = new AmazonDynamoDbClientFactory();
            return factory.createAmazonDynamoDb(dynamoDbProperties);
        }
    }

}
