package org.apereo.cas.config;

import com.mongodb.Mongo;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mongo.ticketregistry.MongoTicketRegistryProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.ticket.registry.MongoDbTicketRegistry;
import org.apereo.cas.ticket.registry.NoOpTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;

/**
 * This is {@link MongoDbTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("mongoTicketRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MongoDbTicketRegistryConfiguration extends AbstractMongoConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("logoutManager")
    private LogoutManager logoutManager;

    @RefreshScope
    @Bean
    public TicketRegistry ticketRegistry() throws Exception {
        final MongoTicketRegistryProperties mongo = casProperties.getTicket().getRegistry().getMongo();
        return new MongoDbTicketRegistry(mongo.getCollectionName(), mongo.isDropCollection(), mongoTemplate());
    }

    @Bean
    public TicketRegistryCleaner ticketRegistryCleaner() throws Exception {
        return new NoOpTicketRegistryCleaner();
    }

    @Override
    protected String getDatabaseName() {
        final MongoTicketRegistryProperties mongo = casProperties.getTicket().getRegistry().getMongo();
        return mongo.getDatabaseName();
    }

    @Override
    public Mongo mongo() throws Exception {
        final MongoTicketRegistryProperties mongo = casProperties.getTicket().getRegistry().getMongo();
        return Beans.newMongoDbClient(mongo);
    }
}
