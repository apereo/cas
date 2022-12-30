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

import java.util.function.Function;

/**
 * This is {@link DynamoDbTicketRegistryTicketCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry, module = "dynamodb")
@AutoConfiguration
public class DynamoDbTicketRegistryTicketCatalogConfiguration {

    @ConditionalOnMissingBean(name = "dynamoDbTicketCatalogConfigurationValuesProvider")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasTicketCatalogConfigurationValuesProvider dynamoDbTicketCatalogConfigurationValuesProvider() {
        return new CasTicketCatalogConfigurationValuesProvider() {
            @Override
            public Function<CasConfigurationProperties, String> getServiceTicketStorageName() {
                return p -> p.getTicket().getRegistry().getDynamoDb().getServiceTicketsTableName();
            }

            @Override
            public Function<CasConfigurationProperties, String> getProxyTicketStorageName() {
                return p -> p.getTicket().getRegistry().getDynamoDb().getProxyTicketsTableName();
            }

            @Override
            public Function<CasConfigurationProperties, String> getTicketGrantingTicketStorageName() {
                return p -> p.getTicket().getRegistry().getDynamoDb().getTicketGrantingTicketsTableName();
            }

            @Override
            public Function<CasConfigurationProperties, String> getProxyGrantingTicketStorageName() {
                return p -> p.getTicket().getRegistry().getDynamoDb().getProxyGrantingTicketsTableName();
            }

            @Override
            public Function<CasConfigurationProperties, String> getTransientSessionStorageName() {
                return p -> p.getTicket().getRegistry().getDynamoDb().getTransientSessionTicketsTableName();
            }
        };
    }
}
