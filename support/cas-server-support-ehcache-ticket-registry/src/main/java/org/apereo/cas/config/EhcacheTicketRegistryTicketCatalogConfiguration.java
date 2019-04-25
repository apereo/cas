package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link EhcacheTicketRegistryTicketCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("ehcacheTicketRegistryTicketCatalogConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class EhcacheTicketRegistryTicketCatalogConfiguration extends TicketDefinitionBuilderSupport {

    public EhcacheTicketRegistryTicketCatalogConfiguration(CasConfigurationProperties casProperties) {
        super(casProperties, new CasTicketCatalogConfigurationValuesProvider() {});
    }
}
