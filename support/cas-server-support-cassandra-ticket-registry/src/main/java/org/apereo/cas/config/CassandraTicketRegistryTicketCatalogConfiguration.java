package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CassandraTicketRegistryTicketCatalogConfiguration}.
 *
 * @author David Rodriguez
 * @since 5.2.0
 */
@Configuration("hazelcastTicketRegistryTicketMetadataCatalogConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CassandraTicketRegistryTicketCatalogConfiguration extends CasCoreTicketCatalogConfiguration {

    /**
     * Name for the ticketGrantingTicket tickets table.
     */
    public static final String TGT_TABLE = "ticketGrantingTicket";

    /**
     * Name for the serviceTicket tickets table.
     */
    public static final String ST_TABLE = "serviceTicket";

    @Override
    protected void buildAndRegisterServiceTicketDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        setServiceTicketDefinitionProperties(metadata);
        super.buildAndRegisterServiceTicketDefinition(plan, metadata);
    }

    @Override
    protected void buildAndRegisterProxyTicketDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        setServiceTicketDefinitionProperties(metadata);
        super.buildAndRegisterServiceTicketDefinition(plan, metadata);
    }

    @Override
    protected void buildAndRegisterTicketGrantingTicketDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        setTicketGrantingTicketProperties(metadata);
        super.buildAndRegisterTicketGrantingTicketDefinition(plan, metadata);
    }

    @Override
    protected void buildAndRegisterProxyGrantingTicketDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        setTicketGrantingTicketProperties(metadata);
        super.buildAndRegisterTicketGrantingTicketDefinition(plan, metadata);
    }

    private void setTicketGrantingTicketProperties(final TicketDefinition metadata) {
        metadata.getProperties().setStorageName(TGT_TABLE);
    }

    private void setServiceTicketDefinitionProperties(final TicketDefinition metadata) {
        metadata.getProperties().setStorageName(ST_TABLE);
    }
}
