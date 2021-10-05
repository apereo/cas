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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CouchDbTicketRegistryConfiguration}.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "couchDbTicketRegistryConfiguration", proxyBeanMethods = false)
public class CouchDbTicketRegistryConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "ticketRegistryCouchDbFactory")
    @Autowired
    public CouchDbConnectorFactory ticketRegistryCouchDbFactory(final CasConfigurationProperties casProperties,
                                                                @Qualifier("defaultObjectMapperFactory")
                                                                final ObjectMapperFactory objectMapperFactory) {
        return new CouchDbConnectorFactory(casProperties.getTicket().getRegistry().getCouchDb(), objectMapperFactory);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "ticketRegistryCouchDbRepository")
    @Autowired
    public TicketRepository ticketRegistryCouchDbRepository(final CasConfigurationProperties casProperties,
                                                            @Qualifier("ticketRegistryCouchDbFactory")
                                                            final CouchDbConnectorFactory ticketRegistryCouchDbFactory) {
        val couchDbProperties = casProperties.getTicket().getRegistry().getCouchDb();
        val ticketRepository = new TicketRepository(ticketRegistryCouchDbFactory.getCouchDbConnector(), couchDbProperties.isCreateIfNotExists());
        ticketRepository.initStandardDesignDocument();
        return ticketRepository;
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "couchDbTicketRegistry")
    @Autowired
    public TicketRegistry ticketRegistry(final CasConfigurationProperties casProperties,
                                         @Qualifier("ticketRegistryCouchDbRepository")
                                         final TicketRepository ticketRegistryCouchDbRepository) {
        val couchDb = casProperties.getTicket().getRegistry().getCouchDb();
        val c = new CouchDbTicketRegistry(ticketRegistryCouchDbRepository, couchDb.getRetries());
        c.setCipherExecutor(CoreTicketUtils.newTicketRegistryCipherExecutor(couchDb.getCrypto(), "couch-db"));
        return c;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "couchDbTicketRegistryCleaner")
    public TicketRegistryCleaner ticketRegistryCleaner() {
        return NoOpTicketRegistryCleaner.getInstance();
    }
}
