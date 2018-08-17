package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.tickets.TicketRepository;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.CouchDbTicketRegistry;
import org.apereo.cas.ticket.registry.NoOpTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.apereo.cas.util.CoreTicketUtils;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CouchDbTicketRegistryConfiguration}.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@Configuration("couchDbTicketRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CouchDbTicketRegistryConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public CouchDbConnectorFactory ticketRegistryCouchDbFactory() {
        return new CouchDbConnectorFactory(casProperties.getTicket().getRegistry().getCouchDb());
    }

    @Bean
    @RefreshScope
    public TicketRepository ticketRegistryCouchDbRepository() {
        val couchDbProperties = casProperties.getTicket().getRegistry().getCouchDb();

        val ticketRepository = new TicketRepository(ticketRegistryCouchDbFactory().create(), couchDbProperties.isCreateIfNotExists());
        ticketRepository.initStandardDesignDocument();
        return ticketRepository;
    }

    @RefreshScope
    @Bean
    @Autowired
    public TicketRegistry ticketRegistry(@Qualifier("ticketCatalog") final TicketCatalog ticketCatalog) {
        val couchDb = casProperties.getTicket().getRegistry().getCouchDb();
        val c = new CouchDbTicketRegistry(ticketCatalog, ticketRegistryCouchDbRepository(), couchDb.getRetries());
        c.setCipherExecutor(CoreTicketUtils.newTicketRegistryCipherExecutor(couchDb.getCrypto(), "couchdb"));
        return c;
    }

    @Bean
    public TicketRegistryCleaner ticketRegistryCleaner() {
        return NoOpTicketRegistryCleaner.getInstance();
    }
}
