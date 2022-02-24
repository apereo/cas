package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnCasFeatureModule;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link EhcacheTicketRegistryTicketCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 * @deprecated Since 6.2, due to Ehcache 2.x being unmaintained. Other registries are available, including Ehcache 3.x.
 */
@Configuration(value = "EhcacheTicketRegistryTicketCatalogConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Deprecated(since = "6.2.0")
@ConditionalOnCasFeatureModule(feature = CasFeatureModule.FeatureCatalog.TicketRegistry, module = "ehcache2")
public class EhcacheTicketRegistryTicketCatalogConfiguration extends BaseTicketDefinitionBuilderSupportConfiguration {

    public EhcacheTicketRegistryTicketCatalogConfiguration(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext) {
        super(casProperties, new CasTicketCatalogConfigurationValuesProvider() {}, applicationContext);
    }
}
