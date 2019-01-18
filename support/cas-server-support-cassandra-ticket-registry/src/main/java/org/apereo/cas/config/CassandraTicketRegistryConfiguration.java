package org.apereo.cas.config;

import org.apereo.cas.cassandra.CassandraSessionFactory;
import org.apereo.cas.cassandra.DefaultCassandraSessionFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.CassandraTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.datastax.driver.core.Session;

@Configuration("cassandraTicketRegistryConfiguration")
@EnableConfigurationProperties({CasConfigurationProperties.class})
public class CassandraTicketRegistryConfiguration {

    private CasConfigurationProperties casConfigurationProperties;
    private CassandraSessionFactory cassandraSessionFactory;

    @Autowired
    public CassandraTicketRegistryConfiguration(CasConfigurationProperties casConfigurationProperties,
                                                CassandraSessionFactory cassandraSessionFactory) {
        this.casConfigurationProperties = casConfigurationProperties;
        this.cassandraSessionFactory = cassandraSessionFactory;
    }

    @Bean
    public TicketRegistry ticketRegistry(TicketCatalog ticketCatalog) {
        return new CassandraTicketRegistry(ticketCatalog, cassandraSessionFactory);
    }

}
