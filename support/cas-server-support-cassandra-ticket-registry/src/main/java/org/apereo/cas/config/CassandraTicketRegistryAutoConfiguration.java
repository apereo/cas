package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.cassandra.CassandraSessionFactory;
import org.apereo.cas.cassandra.DefaultCassandraSessionFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.catalog.CasTicketCatalogConfigurationValuesProvider;
import org.apereo.cas.ticket.registry.CassandraTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import java.util.function.Function;

/**
 * This is {@link CassandraTicketRegistryAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @author doomviking
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry, module = "cassandra")
@AutoConfiguration
public class CassandraTicketRegistryAutoConfiguration {

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
    
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TicketRegistry ticketRegistry(
        @Qualifier(TicketCatalog.BEAN_NAME)
        final TicketCatalog ticketCatalog,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("cassandraTicketRegistrySessionFactory")
        final CassandraSessionFactory cassandraTicketRegistrySessionFactory,
        @Qualifier(TicketSerializationManager.BEAN_NAME)
        final TicketSerializationManager ticketSerializationManager) {
        val cassandra = casProperties.getTicket().getRegistry().getCassandra();
        val cipher = CoreTicketUtils.newTicketRegistryCipherExecutor(cassandra.getCrypto(), "cassandra");
        return new CassandraTicketRegistry(cipher, ticketSerializationManager, ticketCatalog, applicationContext,
            cassandraTicketRegistrySessionFactory, cassandra);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "cassandraTicketRegistrySessionFactory")
    public CassandraSessionFactory cassandraTicketRegistrySessionFactory(
        final CasConfigurationProperties casProperties,
        @Qualifier(CasSSLContext.BEAN_NAME)
        final CasSSLContext casSslContext) {
        val cassandra = casProperties.getTicket().getRegistry().getCassandra();
        return new DefaultCassandraSessionFactory(cassandra, casSslContext.getSslContext());
    }
}
