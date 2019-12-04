package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.tickets.TicketRepository;
import org.apereo.cas.ticket.registry.CouchDbTicketRegistry;
import org.apereo.cas.ticket.registry.NoOpTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.apereo.cas.util.CoreTicketUtils;

import lombok.val;
import org.ektorp.impl.ObjectMapperFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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

    @Autowired
    @Qualifier("defaultObjectMapperFactory")
    private ObjectProvider<ObjectMapperFactory> objectMapperFactory;

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "ticketRegistryCouchDbFactory")
    public CouchDbConnectorFactory ticketRegistryCouchDbFactory() {
        return new CouchDbConnectorFactory(casProperties.getTicket().getRegistry().getCouchDb(), objectMapperFactory.getObject());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "ticketRegistryCouchDbRepository")
    public TicketRepository ticketRegistryCouchDbRepository() {
        val couchDbProperties = casProperties.getTicket().getRegistry().getCouchDb();

        val ticketRepository = new TicketRepository(ticketRegistryCouchDbFactory().getCouchDbConnector(), couchDbProperties.isCreateIfNotExists());
        ticketRepository.initStandardDesignDocument();
        return ticketRepository;
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "couchDbTicketRegistry")
    public TicketRegistry ticketRegistry() {
        val couchDb = casProperties.getTicket().getRegistry().getCouchDb();
        val c = new CouchDbTicketRegistry(ticketRegistryCouchDbRepository(), couchDb.getRetries());
        c.setCipherExecutor(CoreTicketUtils.newTicketRegistryCipherExecutor(couchDb.getCrypto(), "couchdb"));
        return c;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "couchDbTicketRegistryCleaner")
    public TicketRegistryCleaner ticketRegistryCleaner() {
        return NoOpTicketRegistryCleaner.getInstance();
    }
}
