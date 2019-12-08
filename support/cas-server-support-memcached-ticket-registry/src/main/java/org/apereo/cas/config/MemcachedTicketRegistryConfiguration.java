package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.memcached.MemcachedPooledClientConnectionFactory;
import org.apereo.cas.memcached.MemcachedUtils;
import org.apereo.cas.ticket.registry.MemcachedTicketRegistry;
import org.apereo.cas.ticket.registry.NoOpTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.serialization.ComponentSerializationPlan;

import lombok.val;
import net.spy.memcached.transcoders.Transcoder;
import org.springframework.beans.factory.ObjectProvider;
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
public class MemcachedTicketRegistryConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("componentSerializationPlan")
    private ObjectProvider<ComponentSerializationPlan> componentSerializationPlan;

    @ConditionalOnMissingBean(name = "memcachedTicketRegistryTranscoder")
    @RefreshScope
    @Bean
    public Transcoder memcachedTicketRegistryTranscoder() {
        val memcached = casProperties.getTicket().getRegistry().getMemcached();
        return MemcachedUtils.newTranscoder(memcached, componentSerializationPlan.getObject().getRegisteredClasses());
    }

    @ConditionalOnMissingBean(name = "memcachedPooledClientConnectionFactory")
    @RefreshScope
    @Bean
    public MemcachedPooledClientConnectionFactory memcachedPooledClientConnectionFactory() {
        val memcached = casProperties.getTicket().getRegistry().getMemcached();
        return new MemcachedPooledClientConnectionFactory(memcached, memcachedTicketRegistryTranscoder());
    }

    @Bean
    @RefreshScope
    public TicketRegistry ticketRegistry() {
        val memcached = casProperties.getTicket().getRegistry().getMemcached();
        val factory = new MemcachedPooledClientConnectionFactory(memcached, memcachedTicketRegistryTranscoder());
        val registry = new MemcachedTicketRegistry(factory.getObjectPool());
        val cipherExecutor = CoreTicketUtils.newTicketRegistryCipherExecutor(memcached.getCrypto(), "memcached");
        registry.setCipherExecutor(cipherExecutor);
        return registry;
    }

    @Bean
    public TicketRegistryCleaner ticketRegistryCleaner() {
        return NoOpTicketRegistryCleaner.getInstance();
    }
}
