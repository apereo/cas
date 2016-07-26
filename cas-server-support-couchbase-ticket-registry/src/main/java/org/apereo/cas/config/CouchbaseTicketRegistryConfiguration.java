package org.apereo.cas.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.ticket.registry.CouchbaseTicketRegistry;
import org.apereo.cas.ticket.registry.DefaultTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;

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
    public CouchbaseClientFactory ticketRegistryCouchbaseClientFactory() {

        final CouchbaseClientFactory factory = new CouchbaseClientFactory();
        factory.setNodes(StringUtils.commaDelimitedListToSet(
                casProperties.getTicket().getRegistry().getCouchbase().getNodeSet()));
        factory.setTimeout(casProperties.getTicket().getRegistry().getCouchbase().getTimeout());
        factory.setBucketName(casProperties.getTicket().getRegistry().getCouchbase().getBucket());
        factory.setPassword(casProperties.getTicket().getRegistry().getCouchbase().getPassword());

        return factory;
    }

    @RefreshScope
    @Bean(name = {"couchbaseTicketRegistry", "ticketRegistry"})
    public CouchbaseTicketRegistry couchbaseTicketRegistry() {
        final CouchbaseTicketRegistry c = new CouchbaseTicketRegistry();
        c.setCouchbaseClientFactory(ticketRegistryCouchbaseClientFactory());
        c.setCipherExecutor(Beans.newTicketRegistryCipherExecutor(
                casProperties.getTicket().getRegistry().getCouchbase().getCrypto()
        ));
        return c;
    }

    @Bean
    public TicketRegistryCleaner ticketRegistryCleaner() {
        return new CouchbaseTicketRegistryCleaner();
    }

    /**
     * The type Couchbase ticket registry cleaner.
     */
    public static class CouchbaseTicketRegistryCleaner extends DefaultTicketRegistryCleaner {
        @Override
        protected boolean isCleanerSupported() {
            return false;
        }
    }
}
