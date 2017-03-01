package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.DefaultTicketDefinition;
import org.apereo.cas.ticket.ProxyGrantingTicketImpl;
import org.apereo.cas.ticket.ProxyTicketImpl;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketImpl;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.TicketMetadataCatalog;
import org.apereo.cas.ticket.TicketMetadataCatalogConfigurer;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * This is {@link CasProtocolCoreTicketMetadataCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casProtocolCoreTicketMetadataRegistrationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasProtocolCoreTicketMetadataCatalogConfiguration implements TicketMetadataCatalogConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasProtocolCoreTicketMetadataCatalogConfiguration.class);

    @Override
    public final void configureTicketMetadataCatalog(final TicketMetadataCatalog plan) {
        LOGGER.debug("Registering core CAS protocol ticket metadata types...");
        
        buildAndRegisterProxyTicketMetadata(plan,
                buildTicketMetadata(plan, ProxyTicket.PROXY_TICKET_PREFIX, ProxyTicketImpl.class, Ordered.HIGHEST_PRECEDENCE));

        buildAndRegisterServiceTicketMetadata(plan,
                buildTicketMetadata(plan, ServiceTicket.PREFIX, ServiceTicketImpl.class, Ordered.HIGHEST_PRECEDENCE));

        buildAndRegisterProxyGrantingTicketMetadata(plan,
                buildTicketMetadata(plan, ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX,
                        ProxyGrantingTicketImpl.class, Ordered.LOWEST_PRECEDENCE));
        
        buildAndRegisterTicketGrantingTicketMetadata(plan,
                buildTicketMetadata(plan, TicketGrantingTicket.PREFIX,
                        TicketGrantingTicketImpl.class, Ordered.LOWEST_PRECEDENCE));
    }

    protected void buildAndRegisterProxyGrantingTicketMetadata(final TicketMetadataCatalog plan, final TicketDefinition metadata) {
        registerTicketMetadata(plan, metadata);
    }

    protected void buildAndRegisterProxyTicketMetadata(final TicketMetadataCatalog plan, final TicketDefinition metadata) {
        registerTicketMetadata(plan, metadata);
    }

    protected void buildAndRegisterServiceTicketMetadata(final TicketMetadataCatalog plan, final TicketDefinition metadata) {
        registerTicketMetadata(plan, metadata);
    }

    protected void buildAndRegisterTicketGrantingTicketMetadata(final TicketMetadataCatalog plan, final TicketDefinition metadata) {
        registerTicketMetadata(plan, metadata);
    }

    private TicketDefinition buildTicketMetadata(final TicketMetadataCatalog plan, final String prefix, final Class impl, final int order) {
        if (plan.containsTicketMetadata(prefix)) {
            return plan.findTicketMetadata(prefix);
        }
        return new DefaultTicketDefinition(impl, prefix, order);
    }

    /**
     * Register ticket metadata.
     *
     * @param plan     the plan
     * @param metadata the metadata
     */
    private void registerTicketMetadata(final TicketMetadataCatalog plan, final TicketDefinition metadata) {
        plan.registerTicketMetadata(metadata);
    }

}
