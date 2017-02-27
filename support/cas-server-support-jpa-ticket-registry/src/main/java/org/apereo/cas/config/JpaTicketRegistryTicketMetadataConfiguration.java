package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.TicketMetadata;
import org.apereo.cas.ticket.TicketMetadataRegistrationPlan;
import org.apereo.cas.ticket.registry.JpaTicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link JpaTicketRegistryTicketMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("hazelcastTicketRegistryMapsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class JpaTicketRegistryTicketMetadataConfiguration extends CasProtocolCoreTicketMetadataRegistrationConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(JpaTicketRegistryTicketMetadataConfiguration.class);

    @Override
    protected void buildAndRegisterTicketGrantingTicketMetadata(final TicketMetadataRegistrationPlan plan, final TicketMetadata metadata) {
        metadata.setProperty(JpaTicketRegistry.TICKET_REGISTRY_PROPERTY_NAME_TGT_CASCADE, Boolean.TRUE);
        super.buildAndRegisterTicketGrantingTicketMetadata(plan, metadata);
    }
}
