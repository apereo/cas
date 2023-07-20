package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.ticket.catalog.CasTicketCatalogConfigurationValuesProvider;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.function.Function;

/**
 * This is {@link JpaTicketRegistryTicketCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry, module = "jpa")
@AutoConfiguration
public class JpaTicketRegistryTicketCatalogConfiguration {

    @ConditionalOnMissingBean(name = "jpaTicketCatalogConfigurationValuesProvider")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasTicketCatalogConfigurationValuesProvider jpaTicketCatalogConfigurationValuesProvider() {
        return new CasTicketCatalogConfigurationValuesProvider() {
            @Override
            public Function<ConfigurableApplicationContext, Boolean> getProxyGrantingTicketCascadeRemovals() {
                return __ -> Boolean.TRUE;
            }

            @Override
            public Function<ConfigurableApplicationContext, Boolean> getTicketGrantingTicketCascadeRemovals() {
                return __ -> Boolean.TRUE;
            }
        };
    }
}
