package org.apereo.cas.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.ticket.registry.CouchbaseTicketRegistry;
import org.apereo.cas.ticket.registry.DefaultTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class CouchbaseTicketRegistryConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;


    @Nullable
    @Autowired(required = false)
    @Qualifier("ticketCipherExecutor")
    private CipherExecutor<byte[], byte[]> cipherExecutor;

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
    @Bean
    public CouchbaseTicketRegistry couchbaseTicketRegistry() {
        final CouchbaseTicketRegistry c = new CouchbaseTicketRegistry();
        c.setCouchbaseClientFactory(ticketRegistryCouchbaseClientFactory());
        c.setCipherExecutor(cipherExecutor);
        return c;
    }

    @Bean
    public TicketRegistryCleaner ticketRegistryCleaner() {
        final DefaultTicketRegistryCleaner c = new DefaultTicketRegistryCleaner() {
            @Override
            protected boolean isCleanerSupported() {
                return false;
            }
        };
        return c;
    }
}
