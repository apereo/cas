package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link MongoDbTicketRegistryTicketCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("mongoDbTicketRegistryTicketCatalogConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MongoDbTicketRegistryTicketCatalogConfiguration extends CasCoreTicketCatalogConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbTicketRegistryTicketCatalogConfiguration.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    protected void buildAndRegisterServiceTicketDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        metadata.getProperties().setStorageName("serviceTicketsCollection");
        metadata.getProperties().setStorageTimeout(casProperties.getTicket().getSt().getTimeToKillInSeconds());
        super.buildAndRegisterServiceTicketDefinition(plan, metadata);
    }

    @Override
    protected void buildAndRegisterProxyTicketDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        metadata.getProperties().setStorageName("proxyTicketsCollection");
        metadata.getProperties().setStorageTimeout(casProperties.getTicket().getSt().getTimeToKillInSeconds());
        super.buildAndRegisterProxyTicketDefinition(plan, metadata);
    }

    @Override
    protected void buildAndRegisterTicketGrantingTicketDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        metadata.getProperties().setStorageName("ticketGrantingTicketsCollection");
        metadata.getProperties().setStorageTimeout(casProperties.getTicket().getTgt().getMaxTimeToLiveInSeconds());
        super.buildAndRegisterTicketGrantingTicketDefinition(plan, metadata);
    }

    @Override
    protected void buildAndRegisterProxyGrantingTicketDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        metadata.getProperties().setStorageName("proxyGrantingTicketsCollection");
        metadata.getProperties().setStorageTimeout(casProperties.getTicket().getTgt().getMaxTimeToLiveInSeconds());
        super.buildAndRegisterProxyGrantingTicketDefinition(plan, metadata);
    }

}
