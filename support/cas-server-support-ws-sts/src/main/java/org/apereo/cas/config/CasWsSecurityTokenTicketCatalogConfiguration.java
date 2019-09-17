package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.BaseTicketCatalogConfigurer;
import org.apereo.cas.ticket.DefaultSecurityTokenTicket;
import org.apereo.cas.ticket.SecurityTokenTicket;
import org.apereo.cas.ticket.TicketCatalog;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * This is {@link CasWsSecurityTokenTicketCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "casWsSecurityTokenTicketCatalogConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasWsSecurityTokenTicketCatalogConfiguration extends BaseTicketCatalogConfigurer {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    public void configureTicketCatalog(final TicketCatalog plan) {
        LOGGER.debug("Registering core WS security token ticket definitions...");
        val definition = buildTicketDefinition(plan, SecurityTokenTicket.PREFIX, DefaultSecurityTokenTicket.class, Ordered.HIGHEST_PRECEDENCE);
        val properties = definition.getProperties();
        properties.setStorageName("wsSecurityTokenTicketsCache");
        properties.setStorageTimeout(casProperties.getTicket().getTgt().getMaxTimeToLiveInSeconds());
        registerTicketDefinition(plan, definition);
    }
}
