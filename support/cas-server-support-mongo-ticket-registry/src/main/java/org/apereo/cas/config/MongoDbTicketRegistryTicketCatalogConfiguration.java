package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

/**
 * This is {@link MongoDbTicketRegistryTicketCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "mongoDbTicketRegistryTicketCatalogConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MongoDbTicketRegistryTicketCatalogConfiguration extends BaseTicketDefinitionBuilderSupportConfiguration {

    public MongoDbTicketRegistryTicketCatalogConfiguration(final CasConfigurationProperties casProperties,
                                                           @Qualifier("mongoDbTicketCatalogConfigurationValuesProvider")
                                                           final CasTicketCatalogConfigurationValuesProvider configProvider) {
        super(casProperties, configProvider);
    }

    @Configuration("mongoDbTicketCatalogConfigValuesProviderConfiguration")
    static class MongoDbTicketCatalogConfigValuesProviderConfiguration {

        @ConditionalOnMissingBean
        @Bean
        @Autowired
        public CasTicketCatalogConfigurationValuesProvider mongoDbTicketCatalogConfigurationValuesProvider(
            @Qualifier("grantingTicketExpirationPolicy")
            final ExpirationPolicyBuilder grantingTicketExpirationPolicy,
            @Qualifier("serviceTicketExpirationPolicy")
            final ExpirationPolicyBuilder serviceTicketExpirationPolicy,
            @Qualifier("proxyTicketExpirationPolicy")
            final ExpirationPolicyBuilder proxyTicketExpirationPolicy,
            @Qualifier("proxyGrantingTicketExpirationPolicy")
            final ExpirationPolicyBuilder proxyGrantingTicketExpirationPolicy,
            @Qualifier("transientSessionTicketExpirationPolicy")
            final ExpirationPolicyBuilder transientSessionTicketExpirationPolicy) {
            return new CasTicketCatalogConfigurationValuesProvider() {
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

                @Override
                public Function<CasConfigurationProperties, Long> getProxyGrantingTicketStorageTimeout() {
                    return p -> proxyGrantingTicketExpirationPolicy.buildTicketExpirationPolicy().getTimeToLive();
                }

                @Override
                public Function<CasConfigurationProperties, Long> getTransientSessionStorageTimeout() {
                    return p -> transientSessionTicketExpirationPolicy.buildTicketExpirationPolicy().getTimeToLive();
                }
            };
        }
    }
}
