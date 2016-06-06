package org.apereo.cas.config;

import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.DefaultHashAlgorithm;
import net.spy.memcached.FailureMode;
import net.spy.memcached.spring.MemcachedClientFactoryBean;
import org.apereo.cas.ticket.registry.DefaultTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.MemCacheTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.apereo.cas.ticket.registry.support.kryo.KryoTranscoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link MemcachedConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("memcachedConfiguration")
public class MemcachedConfiguration {

    /**
     * The Servers.
     */
    @Value("${memcached.servers:localhost:11211}")
    private String servers;

    /**
     * The Failure mode.
     */
    @Value("${memcached.failureMode:Redistribute}")
    private FailureMode failureMode;

    /**
     * The Locator type.
     */
    @Value("${memcached.locatorType:ARRAY_MOD}")
    private ConnectionFactoryBuilder.Locator locatorType;

    /**
     * The Hash algorithm.
     */
    @Value("net.spy.memcached.DefaultHashAlgorithm.${memcached.hashAlgorithm:FNV1_64_HASH}")
    private DefaultHashAlgorithm hashAlgorithm;

    /**
     * Memcached client memcached client factory bean.
     *
     * @return the memcached client factory bean
     */
    @RefreshScope
    @Bean
    public MemcachedClientFactoryBean memcachedClient() {
        final MemcachedClientFactoryBean bean = new MemcachedClientFactoryBean();
        bean.setServers(this.servers);
        bean.setLocatorType(this.locatorType);
        bean.setTranscoder(kryoTranscoder());
        bean.setFailureMode(this.failureMode);
        bean.setHashAlg(this.hashAlgorithm);
        return bean;
    }
    
    @Bean
    public KryoTranscoder kryoTranscoder() {
        return new KryoTranscoder();
    }
    
    @Bean
    public TicketRegistry memcachedTicketRegistry() {
        return new MemCacheTicketRegistry();
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
