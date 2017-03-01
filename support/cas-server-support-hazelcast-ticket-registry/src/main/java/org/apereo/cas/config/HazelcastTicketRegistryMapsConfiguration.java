package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.hazelcast.HazelcastProperties;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketMetadataCatalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link HazelcastTicketRegistryMapsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("hazelcastTicketRegistryMapsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class HazelcastTicketRegistryMapsConfiguration extends CasProtocolCoreTicketMetadataCatalogConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    protected void buildAndRegisterServiceTicketMetadata(final TicketMetadataCatalog plan, final TicketDefinition metadata) {
        final HazelcastProperties hz = casProperties.getTicket().getRegistry().getHazelcast();
        metadata.getProperties().setCacheName(hz.getServiceTicketsMapName());
        super.buildAndRegisterServiceTicketMetadata(plan, metadata);
    }

    @Override
    protected void buildAndRegisterTicketGrantingTicketMetadata(final TicketMetadataCatalog plan, final TicketDefinition metadata) {
        final HazelcastProperties hz = casProperties.getTicket().getRegistry().getHazelcast();
        metadata.getProperties().setCacheName(hz.getTicketGrantingTicketsMapName());
        super.buildAndRegisterTicketGrantingTicketMetadata(plan, metadata);
    }
}
