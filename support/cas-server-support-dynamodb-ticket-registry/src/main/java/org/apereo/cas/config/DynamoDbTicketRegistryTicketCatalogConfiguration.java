package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

/**
 * This is {@link DynamoDbTicketRegistryTicketCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "dynamoDbTicketRegistryTicketCatalogConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DynamoDbTicketRegistryTicketCatalogConfiguration extends BaseTicketDefinitionBuilderSupportConfiguration {

    public DynamoDbTicketRegistryTicketCatalogConfiguration(final CasConfigurationProperties casProperties,
                                                            @Qualifier("dynamoDbTicketCatalogConfigurationValuesProvider")
                                                            final CasTicketCatalogConfigurationValuesProvider configProvider) {
        super(casProperties, configProvider);
    }

    @Configuration("dynamoDbTicketCatalogConfigValuesProviderConfiguration")
    static class Config {

        @ConditionalOnMissingBean
        @Bean
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
}
