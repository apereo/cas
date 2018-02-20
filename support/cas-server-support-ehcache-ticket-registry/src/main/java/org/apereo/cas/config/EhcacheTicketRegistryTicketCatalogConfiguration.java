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
 * This is {@link EhcacheTicketRegistryTicketCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("ehcacheTicketRegistryTicketCatalogConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class EhcacheTicketRegistryTicketCatalogConfiguration extends CasCoreTicketCatalogConfiguration {
    /**
     * Service ticket cache name.
     */
    public static final String SERVICE_TICKETS_CACHE = "serviceTicketsCache";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EhcacheTicketRegistryTicketCatalogConfiguration.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    protected void buildAndRegisterServiceTicketDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        metadata.getProperties().setStorageName(SERVICE_TICKETS_CACHE);
        metadata.getProperties().setStorageTimeout(casProperties.getTicket().getSt().getTimeToKillInSeconds());
        super.buildAndRegisterServiceTicketDefinition(plan, metadata);
    }

    @Override
    protected void buildAndRegisterProxyTicketDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        metadata.getProperties().setStorageName("proxyTicketsCache");
        metadata.getProperties().setStorageTimeout(casProperties.getTicket().getPt().getTimeToKillInSeconds());
        super.buildAndRegisterProxyTicketDefinition(plan, metadata);
    }

    @Override
    protected void buildAndRegisterTicketGrantingTicketDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        metadata.getProperties().setStorageName("ticketGrantingTicketsCache");
        metadata.getProperties().setStorageTimeout(casProperties.getTicket().getTgt().getMaxTimeToLiveInSeconds());
        super.buildAndRegisterTicketGrantingTicketDefinition(plan, metadata);
    }

    @Override
    protected void buildAndRegisterProxyGrantingTicketDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        metadata.getProperties().setStorageName("proxyGrantingTicketsCache");
        metadata.getProperties().setStorageTimeout(casProperties.getTicket().getTgt().getMaxTimeToLiveInSeconds());
        super.buildAndRegisterProxyGrantingTicketDefinition(plan, metadata);
    }
}
