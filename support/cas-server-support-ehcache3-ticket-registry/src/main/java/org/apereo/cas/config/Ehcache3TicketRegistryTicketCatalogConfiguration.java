package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link Ehcache3TicketRegistryTicketCatalogConfiguration}.
 *
 * @author Hal Deadman
 * @since 6.2.0
 */
@Configuration(value = "Ehcache3TicketRegistryTicketCatalogConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.TicketRegistry, module = "ehcache3")
public class Ehcache3TicketRegistryTicketCatalogConfiguration extends BaseTicketDefinitionBuilderSupportConfiguration {

    public Ehcache3TicketRegistryTicketCatalogConfiguration(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext) {
        super(casProperties, new CasTicketCatalogConfigurationValuesProvider() {}, applicationContext);
    }
}
