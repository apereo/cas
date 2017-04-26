package org.apereo.cas.config;

import com.google.common.collect.ImmutableSet;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.distribution.RMIBootstrapCacheLoader;
import net.sf.ehcache.distribution.RMISynchronousCacheReplicator;
import org.apereo.cas.configuration.CasConfigurationProperties;
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

        return new RMISynchronousCacheReplicator(
                casProperties.getTicket().getRegistry().getEhcache().isReplicatePuts(),
                casProperties.getTicket().getRegistry().getEhcache().isReplicatePutsViaCopy(),
                casProperties.getTicket().getRegistry().getEhcache().isReplicateUpdates(),
                casProperties.getTicket().getRegistry().getEhcache().isReplicateUpdatesViaCopy(),
                casProperties.getTicket().getRegistry().getEhcache().isReplicateRemovals());
    }
    
    @RefreshScope
    @Bean
    public RMIBootstrapCacheLoader ticketCacheBootstrapCacheLoader() {
        return new RMIBootstrapCacheLoader(casProperties.getTicket().getRegistry().getEhcache().isLoaderAsync(),
                casProperties.getTicket().getRegistry().getEhcache().getMaxChunkSize());
    }
    
    @Lazy
    @Bean
    public EhCacheManagerFactoryBean cacheManager() {
        final EhCacheManagerFactoryBean bean = new EhCacheManagerFactoryBean();
        bean.setConfigLocation(casProperties.getTicket().getRegistry().getEhcache().getConfigLocation());
        bean.setShared(casProperties.getTicket().getRegistry().getEhcache().isShared());
        bean.setCacheManagerName(casProperties.getTicket().getRegistry().getEhcache().getCacheManagerName());
        return bean;
    }

    @Lazy
    @Bean
    public EhCacheFactoryBean ehcacheTicketsCache(@Qualifier("cacheManager")
                                                  final CacheManager manager) {
        final EhCacheFactoryBean bean = new EhCacheFactoryBean();
        bean.setCacheName(casProperties.getTicket().getRegistry().getEhcache().getCacheName());
        bean.setCacheEventListeners(ImmutableSet.of(ticketRMISynchronousCacheReplicator()));
        bean.setTimeToIdle(casProperties.getTicket().getRegistry().getEhcache().getCacheTimeToIdle());
        bean.setTimeToLive(casProperties.getTicket().getRegistry().getEhcache().getCacheTimeToLive());

        bean.setCacheManager(manager);
        bean.setBootstrapCacheLoader(ticketCacheBootstrapCacheLoader());
        bean.setDiskExpiryThreadIntervalSeconds(
                casProperties.getTicket().getRegistry().getEhcache().getDiskExpiryThreadIntervalSeconds());
        
        bean.setEternal(casProperties.getTicket().getRegistry().getEhcache().isEternal());
        bean.setMaxEntriesLocalHeap(casProperties.getTicket().getRegistry().getEhcache().getMaxElementsInMemory());
        bean.setMaxEntriesInCache(casProperties.getTicket().getRegistry().getEhcache().getMaxElementsInCache());
        bean.setMaxEntriesLocalDisk(casProperties.getTicket().getRegistry().getEhcache().getMaxElementsOnDisk());
        bean.setMemoryStoreEvictionPolicy(casProperties.getTicket().getRegistry().getEhcache().getMemoryStoreEvictionPolicy());
        
        final PersistenceConfiguration c = new PersistenceConfiguration();
        c.strategy(casProperties.getTicket().getRegistry().getEhcache().getPersistence());
        c.setSynchronousWrites(casProperties.getTicket().getRegistry().getEhcache().isSynchronousWrites());
        bean.persistence(c);
        
        return bean;
    }

    @RefreshScope
    @Bean
    public TicketRegistry ticketRegistry(@Qualifier("ehcacheTicketsCache")
                                                final Cache ehcacheTicketsCache) {
        final EhCacheTicketRegistry r = new EhCacheTicketRegistry(ehcacheTicketsCache);
        r.setCipherExecutor(Beans.newTicketRegistryCipherExecutor(
                casProperties.getTicket().getRegistry().getEhcache().getCrypto()
        ));
        return r;
    }
}
