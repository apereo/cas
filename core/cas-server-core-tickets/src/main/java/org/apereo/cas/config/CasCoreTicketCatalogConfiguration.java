package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
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

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * This is {@link CasCoreTicketCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "casCoreTicketCatalogConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasCoreTicketCatalogConfiguration extends BaseTicketCatalogConfigurer {
    @Override
    public final void configureTicketCatalog(final TicketCatalog plan) {
        LOGGER.trace("Registering core CAS protocol ticket definitions...");

        buildAndRegisterProxyTicketDefinition(plan,
            buildTicketDefinition(plan, ProxyTicket.PROXY_TICKET_PREFIX,
                ProxyTicketImpl.class, Ordered.HIGHEST_PRECEDENCE));

        buildAndRegisterServiceTicketDefinition(plan,
            buildTicketDefinition(plan, ServiceTicket.PREFIX,
                ServiceTicketImpl.class, Ordered.HIGHEST_PRECEDENCE));

        buildAndRegisterProxyGrantingTicketDefinition(plan,
            buildTicketDefinition(plan, ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX,
                ProxyGrantingTicketImpl.class, Ordered.LOWEST_PRECEDENCE));

        buildAndRegisterTicketGrantingTicketDefinition(plan,
            buildTicketDefinition(plan, TicketGrantingTicket.PREFIX,
                TicketGrantingTicketImpl.class, Ordered.LOWEST_PRECEDENCE));

        buildAndRegisterTransientSessionTicketDefinition(plan,
            buildTicketDefinition(plan, TransientSessionTicket.PREFIX,
                TransientSessionTicketImpl.class, Ordered.LOWEST_PRECEDENCE));
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
