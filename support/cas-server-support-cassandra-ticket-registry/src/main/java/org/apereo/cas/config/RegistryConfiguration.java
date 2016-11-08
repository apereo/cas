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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration("ticketRegistryConfiguration")
@PropertySource("classpath:/cassandra.properties")
@EnableScheduling
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RegistryConfiguration {

    @Bean(name = "cassandraDao")
    public NoSqlTicketRegistryDao cassandraJSONDao(@Value("${cassandra.contact.points:localhost}") final String contactPoints, @Value("${tgt.maxRememberMeTimeoutExpiration:5184000}") final int maxTicketDuration, @Value("${cassandra.username}") final String username, @Value("${cassandra.password}") final String password, final ExpirationCalculator calculator) {
        return new CassandraDao<String>(contactPoints, maxTicketDuration, username, password, calculator, new JacksonJSONSerializer(), String.class);
    }

    @Bean(name = {"noSqlTicketRegistry", "ticketRegistry"})
    public TicketRegistry noSqlTicketRegistry(final NoSqlTicketRegistryDao cassandraDao, @Qualifier("logoutManager") final LogoutManager logoutManager, @Value("true") final boolean logUserOutOfServices) {
        return new NoSqlTicketRegistry(cassandraDao, logoutManager, logUserOutOfServices);
    }

    @Bean(name="ticketRegistryCleaner")
    public TicketRegistryCleaner ticketRegistryCleaner(final NoSqlTicketRegistry ticketRegistry) {
        return ticketRegistry;
    }
}
