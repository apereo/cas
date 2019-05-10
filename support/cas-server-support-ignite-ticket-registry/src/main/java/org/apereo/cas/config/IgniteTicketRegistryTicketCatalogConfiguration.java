package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link IgniteTicketRegistryTicketCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("igniteTicketRegistryTicketCatalogConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class IgniteTicketRegistryTicketCatalogConfiguration extends TicketDefinitionBuilderSupport {

    public IgniteTicketRegistryTicketCatalogConfiguration(final CasConfigurationProperties casProperties) {
        super(casProperties, new CasTicketCatalogConfigurationValuesProvider() {});
    }
}
