package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.hazelcast.HazelcastProperties;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketMetadataCatalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link HazelcastTicketRegistryTicketCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("hazelcastTicketRegistryTicketMetadataCatalogConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class HazelcastTicketRegistryTicketCatalogConfiguration extends CasCoreTicketCatalogConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    protected void buildAndRegisterServiceTicketDefinition(final TicketMetadataCatalog plan, final TicketDefinition metadata) {
        setServiceTicketDefinitionProperties(plan, metadata);
        super.buildAndRegisterServiceTicketDefinition(plan, metadata);
    }

    @Override
    protected void buildAndRegisterProxyTicketDefinition(final TicketMetadataCatalog plan, final TicketDefinition metadata) {
        setServiceTicketDefinitionProperties(plan, metadata);
        super.buildAndRegisterServiceTicketDefinition(plan, metadata);
    }
    
    @Override
    protected void buildAndRegisterTicketGrantingTicketDefinition(final TicketMetadataCatalog plan, final TicketDefinition metadata) {
        setTicketGrantingTicketProperties(metadata);
        super.buildAndRegisterTicketGrantingTicketDefinition(plan, metadata);
    }

    @Override
    protected void buildAndRegisterProxyGrantingTicketDefinition(final TicketMetadataCatalog plan, final TicketDefinition metadata) {
        setTicketGrantingTicketProperties(metadata);
        super.buildAndRegisterTicketGrantingTicketDefinition(plan, metadata);
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
