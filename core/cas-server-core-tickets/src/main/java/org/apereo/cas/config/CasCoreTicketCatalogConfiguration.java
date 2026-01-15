package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.ticket.TicketCatalogConfigurer;
import org.apereo.cas.ticket.catalog.CasTicketCatalogConfigurationValuesProvider;
import org.apereo.cas.ticket.catalog.DefaultTicketCatalogConfigurer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasCoreTicketCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry)
@Configuration(value = "CasCoreTicketCatalogConfiguration", proxyBeanMethods = false)
class CasCoreTicketCatalogConfiguration {
    @ConditionalOnMissingBean(name = "casCoreTicketCatalogConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TicketCatalogConfigurer casCoreTicketCatalogConfigurer(
        final CasConfigurationProperties casProperties,
        final ObjectProvider<@NonNull CasTicketCatalogConfigurationValuesProvider> valuesProvider,
        final ConfigurableApplicationContext applicationContext) {
        return new DefaultTicketCatalogConfigurer(casProperties, applicationContext, valuesProvider);
    }
}
