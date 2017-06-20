package org.apereo.cas.config;

import org.apereo.cas.TicketSerializer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.CassandraTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.CassandraTicketRegistry;
import org.apereo.cas.ticket.registry.CassandraTicketRegistryDao;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.serializer.JacksonJsonSerializer;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
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
@EnableConfigurationProperties({CasConfigurationProperties.class, CassandraProperties.class})
public class CassandraTicketRegistryConfiguration {

    @Autowired
    private CassandraProperties cassandraProperties;

    @Bean(name = {"ticketRegistry", "cassandraTicketRegistry"})
    public TicketRegistry cassandraTicketRegistry(@Qualifier("ticketSerializer") final TicketSerializer ticketSerializer,
                                                  @Qualifier("ticketCatalog") final TicketCatalog ticketCatalog) {
        return new CassandraTicketRegistry<>(ticketCatalog, cassandraProperties.getContactPoints(), cassandraProperties.getUsername(),
                cassandraProperties.getPassword(), ticketSerializer, String.class, cassandraProperties.getTgtTable(), cassandraProperties.getStTable(),
                cassandraProperties.getExpiryTable(), cassandraProperties.getLastRunTable());
    }

    @Bean(name = "ticketRegistryCleaner")
    public TicketRegistryCleaner cassandraTicketRegistryCleaner(@Qualifier("cassandraTicketRegistry") final CassandraTicketRegistryDao ticketRegistry,
                                                                @Qualifier("logoutManager") final LogoutManager logoutManager) {
        return new CassandraTicketRegistryCleaner(ticketRegistry, logoutManager);
    }

    @Bean(name = "ticketSerializer")
    public TicketSerializer<String> ticketSerializer() {
        return new JacksonJsonSerializer();
    }
}
