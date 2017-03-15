package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.BaseTicketCatalogConfigurer;
import org.apereo.cas.ticket.DefaultSecurityTokenTicket;
import org.apereo.cas.ticket.SecurityTokenTicket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasWsSecurityTokenTicketCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casWsSecurityTokenTicketCatalogConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasWsSecurityTokenTicketCatalogConfiguration extends BaseTicketCatalogConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasWsSecurityTokenTicketCatalogConfiguration.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    public void configureTicketCatalog(final TicketCatalog plan) {
        LOGGER.debug("Registering core WS security token ticket definitions...");
        final TicketDefinition defn = buildTicketDefinition(plan, SecurityTokenTicket.PREFIX, DefaultSecurityTokenTicket.class);
        defn.getProperties().setStorageName("wsSecurityTokenTicketsCache");
        defn.getProperties().setStorageTimeout(casProperties.getTicket().getTgt().getMaxTimeToLiveInSeconds());
        registerTicketDefinition(plan, defn);
    }
}
