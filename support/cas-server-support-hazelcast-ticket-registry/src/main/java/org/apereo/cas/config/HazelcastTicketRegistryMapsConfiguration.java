package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.hazelcast.HazelcastProperties;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketMetadata;
import org.apereo.cas.ticket.TicketMetadataCatalogRegistrationPlan;
import org.apereo.cas.ticket.registry.HazelcastTicketRegistry;
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
public class HazelcastTicketRegistryMapsConfiguration extends CasProtocolCoreTicketMetadataRegistrationConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    protected void registerTicketMetadata(final TicketMetadataCatalogRegistrationPlan plan, final TicketMetadata metadata) {
        final HazelcastProperties hz = casProperties.getTicket().getRegistry().getHazelcast();

        if (metadata.getImplementationClass().isInstance(TicketGrantingTicket.class)) {
            metadata.setProperty(HazelcastTicketRegistry.HAZELCAST_PROPERTY_NAME_MAP, hz.getTicketGrantingTicketsMapName());
        }
        if (metadata.getImplementationClass().isInstance(ServiceTicket.class)) {
            metadata.setProperty(HazelcastTicketRegistry.HAZELCAST_PROPERTY_NAME_MAP, hz.getServiceTicketsMapName());
        }
        super.registerTicketMetadata(plan, metadata);
    }
}
