package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.hazelcast.HazelcastProperties;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketMetadataCatalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link HazelcastTicketRegistryTicketMetadataCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("hazelcastTicketRegistryTicketMetadataCatalogConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class HazelcastTicketRegistryTicketMetadataCatalogConfiguration extends CasProtocolCoreTicketMetadataCatalogConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    protected void buildAndRegisterServiceTicketMetadata(final TicketMetadataCatalog plan, final TicketDefinition metadata) {
        setServiceTicketDefinitionProperties(plan, metadata);
        super.buildAndRegisterServiceTicketMetadata(plan, metadata);
    }

    @Override
    protected void buildAndRegisterProxyTicketMetadata(final TicketMetadataCatalog plan, final TicketDefinition metadata) {
        setServiceTicketDefinitionProperties(plan, metadata);
        super.buildAndRegisterServiceTicketMetadata(plan, metadata);
    }
    
    @Override
    protected void buildAndRegisterTicketGrantingTicketMetadata(final TicketMetadataCatalog plan, final TicketDefinition metadata) {
        setTicketGrantingTicketProperties(metadata);
        super.buildAndRegisterTicketGrantingTicketMetadata(plan, metadata);
    }

    @Override
    protected void buildAndRegisterProxyGrantingTicketMetadata(final TicketMetadataCatalog plan, final TicketDefinition metadata) {
        setTicketGrantingTicketProperties(metadata);
        super.buildAndRegisterTicketGrantingTicketMetadata(plan, metadata);
    }

    private void setTicketGrantingTicketProperties(final TicketDefinition metadata) {
        final HazelcastProperties hz = casProperties.getTicket().getRegistry().getHazelcast();
        metadata.getProperties().setCacheName(hz.getTicketGrantingTicketsMapName());
    }


    private void setServiceTicketDefinitionProperties(final TicketMetadataCatalog plan, final TicketDefinition metadata) {
        final HazelcastProperties hz = casProperties.getTicket().getRegistry().getHazelcast();
        metadata.getProperties().setCacheName(hz.getServiceTicketsMapName());
    }
}
