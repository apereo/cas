package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.dao.CassandraDao;
import org.apereo.cas.dao.ExpirationCalculator;
import org.apereo.cas.dao.NoSqlTicketRegistry;
import org.apereo.cas.dao.NoSqlTicketRegistryDao;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.serializer.JacksonJSONSerializer;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration("ticketRegistryConfiguration")
@EnableScheduling
@EnableConfigurationProperties({CasConfigurationProperties.class, CassandraProperties.class, ExpirationCalculatorProperties.class})
public class RegistryConfiguration {

    @Autowired CassandraProperties cassandraProperties;

    @Autowired ExpirationCalculatorProperties expirationProperties;

    @Bean(name = "cassandraDao")
    public NoSqlTicketRegistryDao cassandraJSONDao(final ExpirationCalculator calculator) {
        return new CassandraDao<>(cassandraProperties.getContactPoints(), expirationProperties.getMaxTicketDuration(), cassandraProperties.getUsername(), cassandraProperties.getPassword(), calculator, new JacksonJSONSerializer(), String.class);
    }

    @Bean(name = {"noSqlTicketRegistry", "ticketRegistry"})
    public TicketRegistry noSqlTicketRegistry(final NoSqlTicketRegistryDao cassandraDao, @Qualifier("logoutManager") final LogoutManager logoutManager) {
        return new NoSqlTicketRegistry(cassandraDao, logoutManager);
    }

    @Bean(name = "ticketRegistryCleaner")
    public TicketRegistryCleaner ticketRegistryCleaner(final NoSqlTicketRegistry ticketRegistry) {
        return ticketRegistry;
    }

    @Bean
    public ExpirationCalculator expirationCalculator() {
        return new ExpirationCalculator(expirationProperties.getMaxTimeToLiveInSeconds(), expirationProperties.getTimeToKillInSeconds(), expirationProperties.getMaxTicketDuration());
    }
}
