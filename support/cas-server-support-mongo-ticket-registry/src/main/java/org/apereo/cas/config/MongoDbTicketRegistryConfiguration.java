package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mongo.ticketregistry.MongoTicketRegistryProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.DefaultTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.MongoDbTicketRegistry;
import org.apereo.cas.ticket.registry.NoOpTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.apereo.cas.ticket.registry.support.LockingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * This is {@link MongoDbTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("mongoTicketRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MongoDbTicketRegistryConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbTicketRegistryConfiguration.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    @Autowired
    public TicketRegistry ticketRegistry(@Qualifier("ticketCatalog") final TicketCatalog ticketCatalog) {
        final MongoTicketRegistryProperties mongo = casProperties.getTicket().getRegistry().getMongo();
        final MongoDbTicketRegistry registry = new MongoDbTicketRegistry(ticketCatalog, mongoDbTicketRegistryTemplate(), mongo.isDropCollection());
        registry.setCipherExecutor(Beans.newTicketRegistryCipherExecutor(mongo.getCrypto(), "mongo"));
        return registry;
    }

    @Autowired
    @Bean
    public TicketRegistryCleaner ticketRegistryCleaner(@Qualifier("lockingStrategy") final LockingStrategy lockingStrategy,
                                                       @Qualifier("logoutManager") final LogoutManager logoutManager,
                                                       @Qualifier("ticketRegistry") final TicketRegistry ticketRegistry) {
        final boolean isCleanerEnabled = casProperties.getTicket().getRegistry().getCleaner().getSchedule().isEnabled();
        if (isCleanerEnabled) {
            LOGGER.debug("Ticket registry cleaner is enabled.");
            return new DefaultTicketRegistryCleaner(lockingStrategy, logoutManager, ticketRegistry);
        }
        LOGGER.debug("Ticket registry cleaner is not enabled. "
                + "Expired tickets are not forcefully collected and cleaned by CAS. It is up to the ticket registry itself to "
                + "clean up tickets based on expiration and eviction policies.");
        return NoOpTicketRegistryCleaner.getInstance();
    }

    @ConditionalOnMissingBean(name = "mongoDbTicketRegistryTemplate")
    @Bean
    public MongoTemplate mongoDbTicketRegistryTemplate() {
        final MongoDbConnectionFactory factory = new MongoDbConnectionFactory();
        final MongoTicketRegistryProperties mongo = casProperties.getTicket().getRegistry().getMongo();
        return factory.buildMongoTemplate(mongo);
    }
}
