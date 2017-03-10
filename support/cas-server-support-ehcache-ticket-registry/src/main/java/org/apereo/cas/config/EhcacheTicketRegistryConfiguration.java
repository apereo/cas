package org.apereo.cas.config;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.distribution.RMIBootstrapCacheLoader;
import net.sf.ehcache.distribution.RMISynchronousCacheReplicator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.util.CryptographyProperties;
import org.apereo.cas.configuration.model.support.ehcache.EhcacheProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.registry.EhCacheTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.Collections;

/**
 * This is {@link EhcacheTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("ehcacheTicketRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class EhcacheTicketRegistryConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public RMISynchronousCacheReplicator ticketRMISynchronousCacheReplicator() {
        final EhcacheProperties cache = casProperties.getTicket().getRegistry().getEhcache();
        return new RMISynchronousCacheReplicator(
                cache.isReplicatePuts(),
                cache.isReplicatePutsViaCopy(),
                cache.isReplicateUpdates(),
                cache.isReplicateUpdatesViaCopy(),
                cache.isReplicateRemovals());
    }

    @RefreshScope
    @Bean
    public RMIBootstrapCacheLoader ticketCacheBootstrapCacheLoader() {
        final EhcacheProperties cache = casProperties.getTicket().getRegistry().getEhcache();
        return new RMIBootstrapCacheLoader(cache.isLoaderAsync(), cache.getMaxChunkSize());
    }

    @Lazy
    @Bean
    public EhCacheManagerFactoryBean cacheManager() {
        final EhcacheProperties cache = casProperties.getTicket().getRegistry().getEhcache();
        final EhCacheManagerFactoryBean bean = new EhCacheManagerFactoryBean();
        bean.setConfigLocation(cache.getConfigLocation());
        bean.setShared(cache.isShared());
        bean.setCacheManagerName(cache.getCacheManagerName());
        return bean;
    }

    @Lazy
    @Autowired
    @Bean
    public EhCacheFactoryBean ehcacheTicketsCache(@Qualifier("cacheManager") final CacheManager manager) {
        final EhcacheProperties ehcacheProperties = casProperties.getTicket().getRegistry().getEhcache();
        final EhCacheFactoryBean bean = new EhCacheFactoryBean();
        bean.setCacheName(ehcacheProperties.getCacheName());
        bean.setCacheEventListeners(Collections.singleton(ticketRMISynchronousCacheReplicator()));
        bean.setTimeToIdle(ehcacheProperties.getCacheTimeToIdle());
        bean.setTimeToLive(ehcacheProperties.getCacheTimeToLive());

        bean.setCacheManager(manager);
        bean.setBootstrapCacheLoader(ticketCacheBootstrapCacheLoader());
        bean.setDiskExpiryThreadIntervalSeconds(ehcacheProperties.getDiskExpiryThreadIntervalSeconds());

        bean.setEternal(ehcacheProperties.isEternal());
        bean.setMaxEntriesLocalHeap(ehcacheProperties.getMaxElementsInMemory());
        bean.setMaxEntriesInCache(ehcacheProperties.getMaxElementsInCache());
        bean.setMaxEntriesLocalDisk(ehcacheProperties.getMaxElementsOnDisk());
        bean.setMemoryStoreEvictionPolicy(ehcacheProperties.getMemoryStoreEvictionPolicy());

        final PersistenceConfiguration c = new PersistenceConfiguration();
        c.strategy(ehcacheProperties.getPersistence());
        c.setSynchronousWrites(ehcacheProperties.isSynchronousWrites());
        bean.persistence(c);

        return bean;
    }

    @Autowired
    @RefreshScope
    @Bean
    public TicketRegistry ticketRegistry(@Qualifier("ehcacheTicketsCache") final Cache ehcacheTicketsCache) {
        final CryptographyProperties crypto = casProperties.getTicket().getRegistry().getEhcache().getCrypto();
        return new EhCacheTicketRegistry(ehcacheTicketsCache, Beans.newTicketRegistryCipherExecutor(crypto));
    }
}
