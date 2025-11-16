package org.apereo.cas.ticket.catalog;

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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;

/**
 * This is {@link DefaultTicketCatalogConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultTicketCatalogConfigurer extends BaseTicketCatalogConfigurer {
    protected final CasConfigurationProperties casProperties;
    protected final ConfigurableApplicationContext applicationContext;
    private final ObjectProvider<@NonNull CasTicketCatalogConfigurationValuesProvider> configurationValuesProvider;
    @Getter
    private final int order = Ordered.HIGHEST_PRECEDENCE;
    
    @Override
    public final void configureTicketCatalog(final TicketCatalog plan,
                                             final CasConfigurationProperties casProperties) {
        LOGGER.trace("Registering CAS ticket definitions...");

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

    protected void buildAndRegisterProxyGrantingTicketDefinition(final TicketCatalog plan, final TicketDefinition ticketDefinition) {
        configurationValuesProvider.ifAvailable(provider -> {
            val properties = ticketDefinition.getProperties();
            properties.setCascadeRemovals(provider.getProxyGrantingTicketCascadeRemovals().apply(applicationContext));
            properties.setStorageName(provider.getProxyGrantingTicketStorageName().apply(casProperties));
            properties.setStorageTimeout(provider.getProxyGrantingTicketStorageTimeout().apply(applicationContext));
        });
        registerTicketDefinition(plan, ticketDefinition);
    }

    protected void buildAndRegisterProxyTicketDefinition(final TicketCatalog plan, final TicketDefinition ticketDefinition) {
        configurationValuesProvider.ifAvailable(provider -> {
            val properties = ticketDefinition.getProperties();
            properties.setStorageName(provider.getProxyTicketStorageName().apply(casProperties));
            properties.setStorageTimeout(provider.getProxyTicketStorageTimeout().apply(applicationContext));
        });
        registerTicketDefinition(plan, ticketDefinition);
    }

    protected void buildAndRegisterServiceTicketDefinition(final TicketCatalog plan, final TicketDefinition ticketDefinition) {
        configurationValuesProvider.ifAvailable(provider -> {
            val properties = ticketDefinition.getProperties();
            properties.setStorageName(provider.getServiceTicketStorageName().apply(casProperties));
            properties.setStorageTimeout(provider.getServiceTicketStorageTimeout().apply(applicationContext));
        });
        registerTicketDefinition(plan, ticketDefinition);
    }

    protected void buildAndRegisterTicketGrantingTicketDefinition(final TicketCatalog plan, final TicketDefinition ticketDefinition) {
        configurationValuesProvider.ifAvailable(provider -> {
            val properties = ticketDefinition.getProperties();
            properties.setCascadeRemovals(provider.getTicketGrantingTicketCascadeRemovals().apply(applicationContext));
            properties.setStorageName(provider.getTicketGrantingTicketStorageName().apply(casProperties));
            properties.setStorageTimeout(provider.getTicketGrantingTicketStorageTimeout().apply(applicationContext));
        });
        registerTicketDefinition(plan, ticketDefinition);
    }

    protected void buildAndRegisterTransientSessionTicketDefinition(final TicketCatalog plan, final TicketDefinition ticketDefinition) {
        configurationValuesProvider.ifAvailable(provider -> {
            val properties = ticketDefinition.getProperties();
            
            properties.setExcludeFromCascade(true);
            properties.setStorageName(provider.getTransientSessionStorageName().apply(casProperties));
            properties.setStorageTimeout(provider.getTransientSessionStorageTimeout().apply(applicationContext));
        });
        registerTicketDefinition(plan, ticketDefinition);
    }
}
