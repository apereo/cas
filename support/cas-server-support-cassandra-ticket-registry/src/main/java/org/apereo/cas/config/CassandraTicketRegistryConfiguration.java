package org.apereo.cas.config;

import org.apereo.cas.cassandra.CassandraSessionFactory;
import org.apereo.cas.cassandra.DefaultCassandraSessionFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.CassandraTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.CoreTicketUtils;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;

/**
 * This is {@link CassandraTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @author doomviking
 * @since 6.1.0
 */
@Configuration("cassandraTicketRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CassandraTicketRegistryConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("ticketSerializationManager")
    private ObjectProvider<TicketSerializationManager> ticketSerializationManager;

    @Autowired
    @Qualifier("sslContext")
    private ObjectProvider<SSLContext> sslContext;

    @Autowired
    @Bean
    @RefreshScope
    public TicketRegistry ticketRegistry(@Qualifier("ticketCatalog") final TicketCatalog ticketCatalog) {
        val cassandra = casProperties.getTicket().getRegistry().getCassandra();
        val sessionFactory = cassandraTicketRegistrySessionFactory();
        val registry = new CassandraTicketRegistry(ticketCatalog, sessionFactory,
            cassandra, ticketSerializationManager.getObject());
        registry.setCipherExecutor(CoreTicketUtils.newTicketRegistryCipherExecutor(cassandra.getCrypto(), "cassandra"));
        return registry;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "cassandraTicketRegistrySessionFactory")
    public CassandraSessionFactory cassandraTicketRegistrySessionFactory() {
        val cassandra = casProperties.getTicket().getRegistry().getCassandra();
        return new DefaultCassandraSessionFactory(cassandra, sslContext.getObject());
    }
}
