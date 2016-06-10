package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.ticket.registry.CouchbaseTicketRegistry;
import org.apereo.cas.ticket.registry.DefaultTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

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


    @RefreshScope
    @Bean
    public CouchbaseClientFactory ticketRegistryCouchbaseClientFactory() {
        
        final CouchbaseClientFactory factory = new CouchbaseClientFactory();
        factory.setNodes(StringUtils.commaDelimitedListToSet(casProperties.getCouchbaseTicketRegistry().getNodeSet()));
        factory.setTimeout(casProperties.getCouchbaseTicketRegistry().getTimeout());
        factory.setBucketName(casProperties.getCouchbaseTicketRegistry().getBucket());
        factory.setPassword(casProperties.getCouchbaseTicketRegistry().getPassword());
        return factory;
    }

    @RefreshScope
    @Bean
    public CouchbaseTicketRegistry couchbaseTicketRegistry() {
        return new CouchbaseTicketRegistry();
    }

    @Bean
    public TicketRegistryCleaner ticketRegistryCleaner() {
        return new DefaultTicketRegistryCleaner() {
            @Override
            protected boolean isCleanerSupported() {
                return false;
            }
        };
    }
}
