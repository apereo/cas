package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.dynamodb.AmazonDynamoDbClientFactory;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.DynamoDbTicketRegistry;
import org.apereo.cas.ticket.registry.DynamoDbTicketRegistryFacilitator;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CoreTicketUtils;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link DynamoDbTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("dynamoDbTicketRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DynamoDbTicketRegistryConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @RefreshScope
    @Bean
    public TicketRegistry ticketRegistry(@Qualifier("ticketCatalog") final TicketCatalog ticketCatalog) {
        val db = casProperties.getTicket().getRegistry().getDynamoDb();
        val crypto = db.getCrypto();
        return new DynamoDbTicketRegistry(CoreTicketUtils.newTicketRegistryCipherExecutor(crypto, "dynamoDb"),
            dynamoDbTicketRegistryFacilitator(ticketCatalog));
    }

    @Autowired
    @RefreshScope
    @Bean
    public DynamoDbTicketRegistryFacilitator dynamoDbTicketRegistryFacilitator(@Qualifier("ticketCatalog") final TicketCatalog ticketCatalog) {
        val db = casProperties.getTicket().getRegistry().getDynamoDb();
        val f = new DynamoDbTicketRegistryFacilitator(ticketCatalog, db, amazonDynamoDbTicketRegistryClient());
        if (!db.isPreventTableCreationOnStartup()) {
            f.createTicketTables(db.isDropTablesOnStartup());
        }
        return f;
    }

    @RefreshScope
    @Bean
    @SneakyThrows
    @ConditionalOnMissingBean(name = "amazonDynamoDbTicketRegistryClient")
    public AmazonDynamoDB amazonDynamoDbTicketRegistryClient() {
        val dynamoDbProperties = casProperties.getTicket().getRegistry().getDynamoDb();
        val factory = new AmazonDynamoDbClientFactory();
        return factory.createAmazonDynamoDb(dynamoDbProperties);
    }
}
