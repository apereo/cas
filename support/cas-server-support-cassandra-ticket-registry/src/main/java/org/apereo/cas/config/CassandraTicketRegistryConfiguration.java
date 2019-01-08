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
    //private CassandraTicketRegistryProperties cassandraTicketRegistryProperties;
    private CassandraSessionFactory cassandraSessionFactory;

    @Autowired
    public CassandraTicketRegistryConfiguration(CasConfigurationProperties casConfigurationProperties,
                                                //CassandraTicketRegistryProperties cassandraTicketRegistryProperties,
                                                CassandraSessionFactory cassandraSessionFactory) {
        this.casConfigurationProperties = casConfigurationProperties;
        //this.cassandraTicketRegistryProperties = cassandraTicketRegistryProperties;
        this.cassandraSessionFactory = cassandraSessionFactory;
    }

//    @Bean
//    public CassandraSessionFactory cassandraSession(CassandraTicketRegistryProperties cassandraTicketRegistryProperties) {
//        casConfigurationProperties.getTicket().getRegistry();//.getCassandra();
//        casConfigurationProperties.getAuthn().getCassandra();
//        return new DefaultCassandraSessionFactory(cassandraTicketRegistryProperties);
//    }

    @Bean
    public TicketRegistry ticketRegistry(TicketCatalog ticketCatalog) {
        return new CassandraTicketRegistry(ticketCatalog, cassandraSessionFactory);
    }

}