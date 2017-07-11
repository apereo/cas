package org.apereo.cas.config;

import org.apereo.cas.TicketSerializer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.cassandra.ticketregistry.CassandraTicketRegistryProperties;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.CassandraTicketRegistry;
import org.apereo.cas.serializer.JacksonJsonSerializer;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author David Rodriguez
 *
 * @since 5.2.0
 */
@Configuration("cassandraTicketRegistryConfiguration")
@EnableScheduling
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CassandraTicketRegistryConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public TicketRegistry ticketRegistry(@Qualifier("cassandraTicketSerializer") final TicketSerializer ticketSerializer,
                                                  @Qualifier("ticketCatalog") final TicketCatalog ticketCatalog) {
        final CassandraTicketRegistryProperties cassandraProperties = casProperties.getTicket().getRegistry().getCassandra();

        return new CassandraTicketRegistry<>(ticketCatalog, cassandraProperties.getContactPoints(), cassandraProperties.getUsername(),
                cassandraProperties.getPassword(), cassandraProperties.getKeyspace(), ticketSerializer, String.class);
    }

    @Bean
    public TicketSerializer<String> cassandraTicketSerializer() {
        return new JacksonJsonSerializer();
    }
}
