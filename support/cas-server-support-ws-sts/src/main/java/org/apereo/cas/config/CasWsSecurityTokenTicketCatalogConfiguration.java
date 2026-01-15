package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.BaseTicketCatalogConfigurer;
import org.apereo.cas.ticket.DefaultSecurityTokenTicket;
import org.apereo.cas.ticket.SecurityTokenTicket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * This is {@link CasWsSecurityTokenTicketCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.WsFederationIdentityProvider)
@Configuration(value = "CasWsSecurityTokenTicketCatalogConfiguration", proxyBeanMethods = false)
class CasWsSecurityTokenTicketCatalogConfiguration extends BaseTicketCatalogConfigurer {

    @Override
    public void configureTicketCatalog(final TicketCatalog plan,
                                       final CasConfigurationProperties casProperties) {
        LOGGER.debug("Registering core WS security token ticket definitions...");
        val definition = buildTicketDefinition(plan, SecurityTokenTicket.PREFIX,
            SecurityTokenTicket.class, DefaultSecurityTokenTicket.class, Ordered.HIGHEST_PRECEDENCE);
        val properties = definition.getProperties();
        properties.setStorageName("wsSecurityTokenTicketsCache");
        val seconds = Beans.newDuration(casProperties.getTicket().getTgt().getPrimary().getMaxTimeToLiveInSeconds()).toSeconds();
        properties.setStorageTimeout(seconds);
        registerTicketDefinition(plan, definition);
    }
}
