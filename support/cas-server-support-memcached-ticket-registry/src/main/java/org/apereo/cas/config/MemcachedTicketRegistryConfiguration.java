package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.memcached.MemcachedTicketRegistryProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.registry.MemCacheTicketRegistry;
import org.apereo.cas.ticket.registry.MemcachedPooledConnectionFactory;
import org.apereo.cas.ticket.registry.NoOpTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link MemcachedTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("memcachedConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MemcachedTicketRegistryConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public TicketRegistry ticketRegistry() {
        final MemcachedPooledConnectionFactory factory = new MemcachedPooledConnectionFactory(casProperties.getTicket().getRegistry().getMemcached());
        final MemCacheTicketRegistry registry = new MemCacheTicketRegistry(factory.getObjectPool());

        final MemcachedTicketRegistryProperties memcached = casProperties.getTicket().getRegistry().getMemcached();
        registry.setCipherExecutor(Beans.newTicketRegistryCipherExecutor(memcached.getCrypto()));
        return registry;
    }

    @Bean
    public TicketRegistryCleaner ticketRegistryCleaner() {
        return new NoOpTicketRegistryCleaner();
    }
}
