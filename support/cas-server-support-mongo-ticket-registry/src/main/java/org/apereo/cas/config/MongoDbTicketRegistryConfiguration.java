package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.DefaultTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.MongoDbTicketRegistry;
import org.apereo.cas.ticket.registry.NoOpTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.apereo.cas.ticket.registry.support.LockingStrategy;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.MongoDbTicketRegistryFacilitator;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.net.ssl.SSLContext;

/**
 * This is {@link MongoDbTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("mongoTicketRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class MongoDbTicketRegistryConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("ticketSerializationManager")
    private ObjectProvider<TicketSerializationManager> ticketSerializationManager;

    @Autowired
    @Qualifier("sslContext")
    private ObjectProvider<SSLContext> sslContext;

    @RefreshScope
    @Bean
    @Autowired
    public TicketRegistry ticketRegistry(@Qualifier("ticketCatalog") final TicketCatalog ticketCatalog) {
        val mongo = casProperties.getTicket().getRegistry().getMongo();
        val mongoTemplate = mongoDbTicketRegistryTemplate();
        val registry = new MongoDbTicketRegistry(ticketCatalog, mongoTemplate, ticketSerializationManager.getObject());
        registry.setCipherExecutor(CoreTicketUtils.newTicketRegistryCipherExecutor(mongo.getCrypto(), "mongo"));
        new MongoDbTicketRegistryFacilitator(ticketCatalog, mongoTemplate, mongo.isDropCollection())
            .createTicketCollections();
        return registry;
    }

    @Autowired
    @Bean
    public TicketRegistryCleaner ticketRegistryCleaner(@Qualifier("lockingStrategy") final LockingStrategy lockingStrategy,
                                                       @Qualifier("logoutManager") final LogoutManager logoutManager,
                                                       @Qualifier("ticketRegistry") final TicketRegistry ticketRegistry) {
        val isCleanerEnabled = casProperties.getTicket().getRegistry().getCleaner().getSchedule().isEnabled();
        if (isCleanerEnabled) {
            LOGGER.debug("Ticket registry cleaner for MongoDb is enabled.");
            return new DefaultTicketRegistryCleaner(lockingStrategy, logoutManager, ticketRegistry);
        }
        LOGGER.debug("Ticket registry cleaner for MongoDb is not enabled. "
            + "Expired tickets are not forcefully collected and cleaned by CAS. It is up to the ticket registry itself to "
            + "clean up tickets based on expiration and eviction policies.");
        return NoOpTicketRegistryCleaner.getInstance();
    }

    @ConditionalOnMissingBean(name = "mongoDbTicketRegistryTemplate")
    @Bean
    @RefreshScope
    public MongoTemplate mongoDbTicketRegistryTemplate() {
        val factory = new MongoDbConnectionFactory(sslContext.getObject());
        val mongo = casProperties.getTicket().getRegistry().getMongo();
        return factory.buildMongoTemplate(mongo);
    }

}
