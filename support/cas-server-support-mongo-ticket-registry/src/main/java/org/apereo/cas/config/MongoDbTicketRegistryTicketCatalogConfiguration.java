package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.ticket.catalog.CasTicketCatalogConfigurationValuesProvider;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link MongoDbTicketRegistryTicketCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry, module = "mongo")
@Configuration(value = "MongoDbTicketRegistryTicketCatalogConfiguration", proxyBeanMethods = false)
class MongoDbTicketRegistryTicketCatalogConfiguration {

    @Configuration(value = "MongoDbTicketRegistryTicketCatalogProviderConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class MongoDbTicketRegistryTicketCatalogProviderConfiguration {
        @ConditionalOnMissingBean(name = "mongoDbTicketCatalogConfigurationValuesProvider")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasTicketCatalogConfigurationValuesProvider mongoDbTicketCatalogConfigurationValuesProvider() {
            return new MongoDbTicketCatalogConfigurationValuesProvider();
        }

        private static final class MongoDbTicketCatalogConfigurationValuesProvider implements CasTicketCatalogConfigurationValuesProvider {

            @Override
            public Function<CasConfigurationProperties, String> getServiceTicketStorageName() {
                return p -> "serviceTicketsCollection";
            }

            @Override
            public Function<CasConfigurationProperties, String> getProxyTicketStorageName() {
                return p -> "proxyTicketsCollection";
            }

            @Override
            public Function<CasConfigurationProperties, String> getTicketGrantingTicketStorageName() {
                return p -> "ticketGrantingTicketsCollection";
            }

            @Override
            public Function<CasConfigurationProperties, String> getProxyGrantingTicketStorageName() {
                return p -> "proxyGrantingTicketsCollection";
            }

            @Override
            public Function<CasConfigurationProperties, String> getTransientSessionStorageName() {
                return p -> "transientSessionTicketsCollection";
            }
        }
    }
}
