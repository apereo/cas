package org.apereo.cas.config;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.ticket.registry.MongoDbTicketRegistry;
import org.apereo.cas.ticket.registry.NoOpLockingStrategy;
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

import java.util.Collections;

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
    @Bean(name = {"mongoTicketRegistry", "ticketRegistry"})
    public TicketRegistry mongoTicketRegistry() {
        return new MongoDbTicketRegistry();
    }

    @Bean
    public TicketRegistryCleaner ticketRegistryCleaner() {
        return new NoOpTicketRegistryCleaner(new NoOpLockingStrategy(), logoutManager, mongoTicketRegistry(), false);
    }

    @Override
    protected String getDatabaseName() {
        return casProperties.getServiceRegistry().getMongo().getServiceRegistryCollection();
    }

    @Override
    public Mongo mongo() throws Exception {
        return new MongoClient(new ServerAddress(
                casProperties.getServiceRegistry().getMongo().getHost(),
                casProperties.getServiceRegistry().getMongo().getPort()),
                Collections.singletonList(
                        MongoCredential.createCredential(
                                casProperties.getServiceRegistry().getMongo().getUserId(),
                                getDatabaseName(),
                                casProperties.getServiceRegistry().getMongo().getUserPassword().toCharArray())),
                mongoClientOptions());
    }
}
