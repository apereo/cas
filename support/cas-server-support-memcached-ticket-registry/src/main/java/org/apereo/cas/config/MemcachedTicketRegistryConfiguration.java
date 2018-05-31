package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import net.spy.memcached.transcoders.Transcoder;
import org.apereo.cas.ComponentSerializationPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.memcached.MemcachedPooledClientConnectionFactory;
import org.apereo.cas.memcached.MemcachedUtils;
import org.apereo.cas.ticket.registry.MemcachedTicketRegistry;
import org.apereo.cas.ticket.registry.NoOpTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.apereo.cas.util.CoreTicketUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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
@Slf4j
public class MemcachedTicketRegistryConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("componentSerializationPlan")
    private ComponentSerializationPlan componentSerializationPlan;

    @ConditionalOnMissingBean(name = "memcachedTicketRegistryTranscoder")
    @RefreshScope
    @Bean
    public Transcoder memcachedTicketRegistryTranscoder() {
        final var memcached = casProperties.getTicket().getRegistry().getMemcached();
        return MemcachedUtils.newTranscoder(memcached, componentSerializationPlan.getRegisteredClasses());
    }

    @ConditionalOnMissingBean(name = "memcachedPooledClientConnectionFactory")
    @RefreshScope
    @Bean
    public MemcachedPooledClientConnectionFactory memcachedPooledClientConnectionFactory() {
        final var memcached = casProperties.getTicket().getRegistry().getMemcached();
        return new MemcachedPooledClientConnectionFactory(memcached, memcachedTicketRegistryTranscoder());
    }

    @Bean
    public TicketRegistry ticketRegistry() {
        final var memcached = casProperties.getTicket().getRegistry().getMemcached();
        final var factory = new MemcachedPooledClientConnectionFactory(memcached, memcachedTicketRegistryTranscoder());
        final var registry = new MemcachedTicketRegistry(factory.getObjectPool());
        final var cipherExecutor = CoreTicketUtils.newTicketRegistryCipherExecutor(memcached.getCrypto(), "memcached");
        registry.setCipherExecutor(cipherExecutor);
        return registry;
    }

    @Bean
    public TicketRegistryCleaner ticketRegistryCleaner() {
        return NoOpTicketRegistryCleaner.getInstance();
    }
}
