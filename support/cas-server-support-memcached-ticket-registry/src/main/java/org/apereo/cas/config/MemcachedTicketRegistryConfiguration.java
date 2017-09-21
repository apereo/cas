package org.apereo.cas.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.memcached.MemcachedTicketRegistryProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.memcached.MemcachedPooledConnectionFactory;
import org.apereo.cas.ticket.registry.MemcachedTicketRegistry;
import org.apereo.cas.ticket.registry.NoOpTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collection;

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

    @Autowired(required = false)
    @Qualifier("kryoSerializableClasses")
    private Collection<Class<?>> kryoSerializableClasses = new ArrayList<>();

    @Bean
    public TicketRegistry ticketRegistry() {
        final MemcachedTicketRegistryProperties memcached = casProperties.getTicket().getRegistry().getMemcached();
        final MemcachedPooledConnectionFactory factory = new MemcachedPooledConnectionFactory(memcached, this.kryoSerializableClasses);
        final MemcachedTicketRegistry registry = new MemcachedTicketRegistry(factory.getObjectPool());
        final CipherExecutor cipherExecutor = Beans.newTicketRegistryCipherExecutor(memcached.getCrypto(), "memcached");
        registry.setCipherExecutor(cipherExecutor);
        return registry;
    }

    @Bean
    public TicketRegistryCleaner ticketRegistryCleaner() {
        return NoOpTicketRegistryCleaner.getInstance();
    }
}
