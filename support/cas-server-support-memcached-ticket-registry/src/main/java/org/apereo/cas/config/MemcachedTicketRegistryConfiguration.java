package org.apereo.cas.config;

import com.google.common.base.Throwables;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.DefaultHashAlgorithm;
import net.spy.memcached.FailureMode;
import net.spy.memcached.MemcachedClientIF;
import net.spy.memcached.spring.MemcachedClientFactoryBean;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.ticket.registry.MemCacheTicketRegistry;
import org.apereo.cas.ticket.registry.NoOpTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.apereo.cas.ticket.registry.support.kryo.KryoTranscoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

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
    @Qualifier("logoutManager")
    private LogoutManager logoutManager;

    @Lazy
    @Bean
    public MemcachedClientFactoryBean memcachedClient() {
        try {
            final MemcachedClientFactoryBean bean = new MemcachedClientFactoryBean();
            bean.setServers(casProperties.getTicket().getRegistry().getMemcached().getServers());
            bean.setLocatorType(ConnectionFactoryBuilder.Locator.valueOf(casProperties.getTicket().getRegistry().getMemcached().getLocatorType()));
            bean.setTranscoder(kryoTranscoder());
            bean.setFailureMode(FailureMode.valueOf(casProperties.getTicket().getRegistry().getMemcached().getFailureMode()));
            bean.setHashAlg(DefaultHashAlgorithm.valueOf(casProperties.getTicket().getRegistry().getMemcached().getHashAlgorithm()));
            return bean;
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Bean
    public KryoTranscoder kryoTranscoder() {
        return new KryoTranscoder();
    }

    @Autowired
    @Bean
    public TicketRegistry ticketRegistry(@Qualifier("memcachedClient") final MemcachedClientIF memcachedClientIF) {
        final MemCacheTicketRegistry registry = new MemCacheTicketRegistry(memcachedClientIF);
        registry.setCipherExecutor(Beans.newTicketRegistryCipherExecutor(casProperties.getTicket().getRegistry().getMemcached().getCrypto()));
        return registry;
    }

    @Bean
    public TicketRegistryCleaner ticketRegistryCleaner() {
        return new NoOpTicketRegistryCleaner();
    }
}
