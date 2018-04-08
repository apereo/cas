package org.apereo.cas.config;

import org.apereo.cas.cassandra.CassandraSessionFactory;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.CassandraTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author David Rodriguez
 *
 * @since 5.2.0
 */
@Configuration("cassandraTicketRegistryConfiguration")
public class CassandraTicketRegistryConfiguration {

    @Bean
    public TicketRegistry ticketRegistry(final CassandraSessionFactory cassandraSessionFactory,
                                         @Qualifier("ticketCatalog") final TicketCatalog ticketCatalog) {

        return new CassandraTicketRegistry(cassandraSessionFactory.getSession(), ticketCatalog);
    }
}
