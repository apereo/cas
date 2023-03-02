package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.ticket.catalog.CasTicketCatalogConfigurationValuesProvider;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CosmosDbTicketRegistryTicketCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry, module = "cosmosdb")
@AutoConfiguration
public class CosmosDbTicketRegistryTicketCatalogConfiguration {

    @ConditionalOnMissingBean(name = "cosmosDbTicketCatalogConfigurationValuesProvider")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasTicketCatalogConfigurationValuesProvider cosmodDbTicketCatalogConfigurationValuesProvider() {
        return new CasTicketCatalogConfigurationValuesProvider() {
        };
    }

}
