package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link Ehcache3TicketRegistryTicketCatalogConfiguration}.
 *
 * @author Hal Deadman
 * @since 6.2.0
 */
@Configuration(value = "ehcache3TicketRegistryTicketCatalogConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class Ehcache3TicketRegistryTicketCatalogConfiguration extends BaseTicketDefinitionBuilderSupportConfiguration {

    public Ehcache3TicketRegistryTicketCatalogConfiguration(final CasConfigurationProperties casProperties) {
        super(casProperties, new CasTicketCatalogConfigurationValuesProvider() {});
    }
}
