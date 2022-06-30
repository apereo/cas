package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link Ehcache3TicketRegistryTicketCatalogConfiguration}.
 *
 * @author Hal Deadman
 * @since 6.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry, module = "ehcache3")
@AutoConfiguration
public class Ehcache3TicketRegistryTicketCatalogConfiguration extends BaseTicketDefinitionBuilderSupportConfiguration {

    public Ehcache3TicketRegistryTicketCatalogConfiguration(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext) {
        super(casProperties, new CasTicketCatalogConfigurationValuesProvider() {}, applicationContext);
    }
}
