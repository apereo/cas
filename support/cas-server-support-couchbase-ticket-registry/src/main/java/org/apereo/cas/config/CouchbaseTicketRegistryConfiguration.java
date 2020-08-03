package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.ticket.registry.CouchbaseTicketRegistry;
import org.apereo.cas.ticket.registry.NoOpTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.apereo.cas.util.CoreTicketUtils;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CouchbaseTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("couchbaseTicketRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CouchbaseTicketRegistryConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "ticketRegistryCouchbaseClientFactory")
    public CouchbaseClientFactory ticketRegistryCouchbaseClientFactory() {
        val cb = casProperties.getTicket().getRegistry().getCouchbase();
        return new CouchbaseClientFactory(cb);
    }

    @RefreshScope
    @Bean
    public TicketRegistry ticketRegistry() {
        val couchbase = casProperties.getTicket().getRegistry().getCouchbase();
        val c = new CouchbaseTicketRegistry(ticketRegistryCouchbaseClientFactory());
        c.setCipherExecutor(CoreTicketUtils.newTicketRegistryCipherExecutor(couchbase.getCrypto(), "couchbase"));
        return c;
    }

    @Bean
    public TicketRegistryCleaner ticketRegistryCleaner() {
        return NoOpTicketRegistryCleaner.getInstance();
    }
}
