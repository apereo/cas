package org.apereo.cas.config;

import net.spy.memcached.MemcachedClientIF;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
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
        final ObjectPool<MemcachedClientIF> pool = new GenericObjectPool<>(factory);
        final MemcachedTicketRegistry registry = new MemcachedTicketRegistry(pool);

        final MemcachedTicketRegistryProperties memcached = casProperties.getTicket().getRegistry().getMemcached();
        final CipherExecutor cipherExecutor = Beans.newTicketRegistryCipherExecutor(memcached.getCrypto(), "memcached");
        registry.setCipherExecutor(cipherExecutor);
        return registry;
    }

    @Bean
    public TicketRegistryCleaner ticketRegistryCleaner() {
        return new NoOpTicketRegistryCleaner();
    }
}
