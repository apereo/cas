package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.ticket.BaseTicketCatalogConfigurer;
import org.apereo.cas.ticket.ProxyGrantingTicketImpl;
import org.apereo.cas.ticket.ProxyTicketImpl;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketImpl;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketImpl;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.Ordered;

/**
 * This is {@link CasCoreTicketCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry)
@AutoConfiguration
public class CasCoreTicketCatalogConfiguration extends BaseTicketCatalogConfigurer {
    @Override
    public final void configureTicketCatalog(final TicketCatalog plan,
                                             final CasConfigurationProperties casProperties) {
        LOGGER.trace("Registering core CAS protocol ticket definitions...");

        buildAndRegisterProxyTicketDefinition(plan,
            buildTicketDefinition(plan, ProxyTicket.PROXY_TICKET_PREFIX,
                ProxyTicket.class, ProxyTicketImpl.class, Ordered.HIGHEST_PRECEDENCE));

        buildAndRegisterServiceTicketDefinition(plan,
            buildTicketDefinition(plan, ServiceTicket.PREFIX,
                ServiceTicket.class, ServiceTicketImpl.class, Ordered.HIGHEST_PRECEDENCE));

        buildAndRegisterProxyGrantingTicketDefinition(plan,
            buildTicketDefinition(plan, ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX,
                ProxyGrantingTicket.class, ProxyGrantingTicketImpl.class, Ordered.LOWEST_PRECEDENCE));

        buildAndRegisterTicketGrantingTicketDefinition(plan,
            buildTicketDefinition(plan, TicketGrantingTicket.PREFIX,
                TicketGrantingTicket.class, TicketGrantingTicketImpl.class, Ordered.LOWEST_PRECEDENCE));

        buildAndRegisterTransientSessionTicketDefinition(plan,
            buildTicketDefinition(plan, TransientSessionTicket.PREFIX,
                TransientSessionTicket.class, TransientSessionTicketImpl.class, Ordered.LOWEST_PRECEDENCE));
    }

    protected void buildAndRegisterProxyGrantingTicketDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        registerTicketDefinition(plan, metadata);
    }

    protected void buildAndRegisterProxyTicketDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        registerTicketDefinition(plan, metadata);
    }

    protected void buildAndRegisterServiceTicketDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        registerTicketDefinition(plan, metadata);
    }

    protected void buildAndRegisterTicketGrantingTicketDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        registerTicketDefinition(plan, metadata);
    }

    protected void buildAndRegisterTransientSessionTicketDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        metadata.getProperties().setExcludeFromCascade(true);
        registerTicketDefinition(plan, metadata);
    }
}
