package org.apereo.cas.config;

import org.apereo.cas.cassandra.CassandraSessionFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.CassandraTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("cassandraTicketRegistryConfiguration")
@EnableConfigurationProperties({CasConfigurationProperties.class})
public class CassandraTicketRegistryConfiguration {

    private CassandraSessionFactory cassandraSessionFactory;

    @Autowired
    public CassandraTicketRegistryConfiguration(final CassandraSessionFactory cassandraSessionFactory) {
        this.cassandraSessionFactory = cassandraSessionFactory;
    }

    @Bean
    public TicketRegistry ticketRegistry(final TicketCatalog ticketCatalog) {
        return new CassandraTicketRegistry(ticketCatalog, cassandraSessionFactory);
    }

}
