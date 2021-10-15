package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.cassandra.CassandraSessionFactory;
import org.apereo.cas.cassandra.DefaultCassandraSessionFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.CassandraTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.CoreTicketUtils;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CassandraTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @author doomviking
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "cassandraTicketRegistryConfiguration", proxyBeanMethods = false)
public class CassandraTicketRegistryConfiguration {

    @Autowired
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TicketRegistry ticketRegistry(
        @Qualifier("ticketCatalog")
        final TicketCatalog ticketCatalog, final CasConfigurationProperties casProperties,
        @Qualifier("cassandraTicketRegistrySessionFactory")
        final CassandraSessionFactory cassandraTicketRegistrySessionFactory,
        @Qualifier("ticketSerializationManager")
        final TicketSerializationManager ticketSerializationManager) {
        val cassandra = casProperties.getTicket().getRegistry().getCassandra();
        val registry = new CassandraTicketRegistry(ticketCatalog, cassandraTicketRegistrySessionFactory,
            cassandra, ticketSerializationManager);
        registry.setCipherExecutor(CoreTicketUtils.newTicketRegistryCipherExecutor(cassandra.getCrypto(), "cassandra"));
        return registry;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "cassandraTicketRegistrySessionFactory")
    @Autowired
    public CassandraSessionFactory cassandraTicketRegistrySessionFactory(
        final CasConfigurationProperties casProperties,
        @Qualifier("casSslContext")
        final CasSSLContext casSslContext) {
        val cassandra = casProperties.getTicket().getRegistry().getCassandra();
        return new DefaultCassandraSessionFactory(cassandra, casSslContext.getSslContext());
    }
}
