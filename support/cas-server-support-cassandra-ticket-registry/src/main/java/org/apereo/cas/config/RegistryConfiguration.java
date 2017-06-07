package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.dao.CassandraDao;
import org.apereo.cas.dao.CassandraTicketRegistryCleaner;
import org.apereo.cas.dao.NoSqlTicketRegistry;
import org.apereo.cas.dao.NoSqlTicketRegistryDao;
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
 * @since 5.1.0
 */
@Configuration("ticketRegistryConfiguration")
@EnableScheduling
@EnableConfigurationProperties({CasConfigurationProperties.class, CassandraProperties.class})
public class RegistryConfiguration {

    @Autowired
    private CassandraProperties cassandraProperties;

    @Bean(name = "cassandraDao")
    public NoSqlTicketRegistryDao cassandraJsonDao() {
        return new CassandraDao<>(cassandraProperties.getContactPoints(), cassandraProperties.getUsername(), cassandraProperties.getPassword(),
                new JacksonJsonSerializer(), String.class, cassandraProperties.getTgtTable(), cassandraProperties.getStTable(),
                cassandraProperties.getExpiryTable(), cassandraProperties.getLastRunTable());
    }

    @Bean(name = {"noSqlTicketRegistry", "ticketRegistry"})
    public TicketRegistry noSqlTicketRegistry(final NoSqlTicketRegistryDao cassandraDao) {
        return new NoSqlTicketRegistry(cassandraDao);
    }

    @Bean(name = "ticketRegistryCleaner")
    public TicketRegistryCleaner ticketRegistryCleaner(final NoSqlTicketRegistryDao cassandraDao,
                                                       @Qualifier("logoutManager") final LogoutManager logoutManager) {
        return new CassandraTicketRegistryCleaner(cassandraDao, logoutManager);
    }
}
