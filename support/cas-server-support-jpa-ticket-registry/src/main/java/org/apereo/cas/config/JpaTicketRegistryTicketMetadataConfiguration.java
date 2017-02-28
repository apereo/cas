package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.TicketMetadata;
import org.apereo.cas.ticket.TicketMetadataRegistrationPlan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static org.apereo.cas.ticket.TicketMetadata.TicketMetadataProperties.*;

/**
 * This is {@link JpaTicketRegistryTicketMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("hazelcastTicketRegistryMapsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class JpaTicketRegistryTicketMetadataConfiguration extends CasProtocolCoreTicketMetadataRegistrationConfiguration {
    @Override
    protected void buildAndRegisterTicketGrantingTicketMetadata(final TicketMetadataRegistrationPlan plan, final TicketMetadata metadata) {
        metadata.setProperty(CASCADE_TICKET, Boolean.TRUE);
        super.buildAndRegisterTicketGrantingTicketMetadata(plan, metadata);
    }
}
