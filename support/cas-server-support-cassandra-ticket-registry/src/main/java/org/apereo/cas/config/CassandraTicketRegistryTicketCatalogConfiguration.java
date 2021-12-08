package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.function.Function;

/**
 * This is {@link CassandraTicketRegistryTicketCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration(value = "CassandraTicketRegistryTicketCatalogConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CassandraTicketRegistryTicketCatalogConfiguration extends BaseTicketDefinitionBuilderSupportConfiguration {

    public CassandraTicketRegistryTicketCatalogConfiguration(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("cassandraTicketCatalogConfigurationValuesProvider")
        final CasTicketCatalogConfigurationValuesProvider configProvider) {
        super(casProperties, configProvider, applicationContext);
    }


    @Configuration(value = "CassandraTicketRegistryTicketCatalogProviderConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CassandraTicketRegistryTicketCatalogProviderConfiguration {
        @ConditionalOnMissingBean(name = "cassandraTicketCatalogConfigurationValuesProvider")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasTicketCatalogConfigurationValuesProvider cassandraTicketCatalogConfigurationValuesProvider() {
            return new CasTicketCatalogConfigurationValuesProvider() {
                @Override
                public Function<CasConfigurationProperties, String> getServiceTicketStorageName() {
                    return p -> "serviceTicketsTable";
                }

                @Override
                public Function<CasConfigurationProperties, String> getProxyTicketStorageName() {
                    return p -> "proxyTicketsTable";
                }

                @Override
                public Function<CasConfigurationProperties, String> getTicketGrantingTicketStorageName() {
                    return p -> "ticketGrantingTicketsTable";
                }

                @Override
                public Function<CasConfigurationProperties, String> getProxyGrantingTicketStorageName() {
                    return p -> "proxyGrantingTicketsTable";
                }

                @Override
                public Function<CasConfigurationProperties, String> getTransientSessionStorageName() {
                    return p -> "transientSessionTicketsTable";
                }
            };
        }
    }
}
