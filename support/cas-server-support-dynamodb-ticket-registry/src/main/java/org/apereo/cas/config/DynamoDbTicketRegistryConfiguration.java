package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.util.CryptographyProperties;
import org.apereo.cas.configuration.model.support.dynamodb.DynamoDbProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.DynamoDbTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
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
public class DynamoDbTicketRegistryConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @RefreshScope
    @Bean
    public TicketRegistry ticketRegistry(@Qualifier("ticketCatalog") final TicketCatalog ticketCatalog) {
        final DynamoDbProperties db = casProperties.getTicket().getRegistry().getDynamoDb();
        final CryptographyProperties crypto = db.getCrypto();
        return new DynamoDbTicketRegistry(Beans.newTicketRegistryCipherExecutor(crypto), ticketCatalog, db);
    }
}
