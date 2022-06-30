package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.couchbase.core.DefaultCouchbaseClientFactory;
import org.apereo.cas.ticket.registry.CouchbaseTicketRegistry;
import org.apereo.cas.ticket.registry.NoOpTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CouchbaseTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 * @deprecated since 6.6
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry, module = "couchbase")
@AutoConfiguration
@Deprecated(since = "6.6")
public class CouchbaseTicketRegistryConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "ticketRegistryCouchbaseClientFactory")
    public CouchbaseClientFactory ticketRegistryCouchbaseClientFactory(final CasConfigurationProperties casProperties) {
        val cb = casProperties.getTicket().getRegistry().getCouchbase();
        return new DefaultCouchbaseClientFactory(cb);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public TicketRegistry ticketRegistry(final CasConfigurationProperties casProperties,
                                         @Qualifier("ticketRegistryCouchbaseClientFactory")
                                         final CouchbaseClientFactory ticketRegistryCouchbaseClientFactory) {
        val couchbase = casProperties.getTicket().getRegistry().getCouchbase();
        val c = new CouchbaseTicketRegistry(ticketRegistryCouchbaseClientFactory);
        c.setCipherExecutor(CoreTicketUtils.newTicketRegistryCipherExecutor(couchbase.getCrypto(), "couchbase"));
        return c;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TicketRegistryCleaner ticketRegistryCleaner() {
        return NoOpTicketRegistryCleaner.getInstance();
    }
}
