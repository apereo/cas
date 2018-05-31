package org.apereo.cas.config;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.dynamodb.AmazonDynamoDbClientFactory;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.DynamoDbTicketRegistry;
import org.apereo.cas.ticket.registry.DynamoDbTicketRegistryFacilitator;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CoreTicketUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@Slf4j
public class DynamoDbTicketRegistryConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @RefreshScope
    @Bean
    public TicketRegistry ticketRegistry(@Qualifier("ticketCatalog") final TicketCatalog ticketCatalog) {
        final var db = casProperties.getTicket().getRegistry().getDynamoDb();
        final var crypto = db.getCrypto();
        return new DynamoDbTicketRegistry(CoreTicketUtils.newTicketRegistryCipherExecutor(crypto, "dynamoDb"),
            dynamoDbTicketRegistryFacilitator(ticketCatalog));
    }

    @Autowired
    @RefreshScope
    @Bean
    public DynamoDbTicketRegistryFacilitator dynamoDbTicketRegistryFacilitator(@Qualifier("ticketCatalog") final TicketCatalog ticketCatalog) {
        final var db = casProperties.getTicket().getRegistry().getDynamoDb();
        final var f = new DynamoDbTicketRegistryFacilitator(ticketCatalog, db, amazonDynamoDbClient());
        if (!db.isPreventTableCreationOnStartup()) {
            f.createTicketTables(db.isDropTablesOnStartup());
        }
        return f;
    }

    @RefreshScope
    @Bean
    @SneakyThrows
    public AmazonDynamoDB amazonDynamoDbClient() {
        final var dynamoDbProperties = casProperties.getTicket().getRegistry().getDynamoDb();
        final var factory = new AmazonDynamoDbClientFactory();
        return factory.createAmazonDynamoDb(dynamoDbProperties);
    }
}
