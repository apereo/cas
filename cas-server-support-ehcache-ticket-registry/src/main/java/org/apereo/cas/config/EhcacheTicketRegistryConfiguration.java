package org.apereo.cas.config;

import com.google.common.collect.ImmutableSet;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.distribution.RMIBootstrapCacheLoader;
import net.sf.ehcache.distribution.RMISynchronousCacheReplicator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.registry.EhCacheTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link EhcacheTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("ehcacheTicketRegistryConfiguration")
public class EhcacheTicketRegistryConfiguration {


    @Autowired
    private CasConfigurationProperties casProperties;

    /**
     * Ticket rmi synchronous cache replicator rmi synchronous cache replicator.
     *
     * @return the rmi synchronous cache replicator
     */
    @RefreshScope
    @Bean
    public RMISynchronousCacheReplicator ticketRMISynchronousCacheReplicator() {
        
        return new RMISynchronousCacheReplicator(casProperties.getEhcache().isReplicatePuts(),
                casProperties.getEhcache().isReplicatePutsViaCopy(),
                casProperties.getEhcache().isReplicateUpdates(),
                casProperties.getEhcache().isReplicateUpdatesViaCopy(),
                casProperties.getEhcache().isReplicateRemovals());
    }

    /**
     * Ticket cache bootstrap cache loader rmi bootstrap cache loader.
     *
     * @return the rmi bootstrap cache loader
     */
    @RefreshScope
    @Bean
    public RMIBootstrapCacheLoader ticketCacheBootstrapCacheLoader() {
        return new RMIBootstrapCacheLoader(casProperties.getEhcache().isLoaderAsync(),
                casProperties.getEhcache().getMaxChunkSize());
    }


    /**
     * Cache manager eh cache manager factory bean.
     *
     * @return the eh cache manager factory bean
     */
    @RefreshScope
    @Bean
    public EhCacheManagerFactoryBean cacheManager() {
        final EhCacheManagerFactoryBean bean = new EhCacheManagerFactoryBean();
        bean.setConfigLocation(ResourceUtils.prepareClasspathResourceIfNeeded(casProperties.getEhcache().getConfigLocation()));
        bean.setShared(casProperties.getEhcache().isShared());
        bean.setCacheManagerName(casProperties.getEhcache().getCacheManagerName());

        return bean;
    }

    /**
     * Service tickets cache eh cache factory bean.
     *
     * @param manager the manager
     * @return the eh cache factory bean
     */
    @RefreshScope
    @Bean
    public EhCacheFactoryBean ehcacheTicketsCache(final CacheManager manager) {
        final EhCacheFactoryBean bean = new EhCacheFactoryBean();
        bean.setCacheName(casProperties.getEhcache().getCacheName());
        bean.setCacheEventListeners(ImmutableSet.of(ticketRMISynchronousCacheReplicator()));
        bean.setTimeToIdle(casProperties.getEhcache().getCacheTimeToIdle());
        bean.setTimeToLive(casProperties.getEhcache().getCacheTimeToLive());

        bean.setCacheManager(manager);
        bean.setBootstrapCacheLoader(ticketCacheBootstrapCacheLoader());

        bean.setDiskExpiryThreadIntervalSeconds(casProperties.getEhcache().getDiskExpiryThreadIntervalSeconds());
        bean.setDiskPersistent(casProperties.getEhcache().isDiskPersistent());
        bean.setEternal(casProperties.getEhcache().isEternal());
        bean.setMaxElementsInMemory(casProperties.getEhcache().getMaxElementsInMemory());
        bean.setMaxElementsOnDisk(casProperties.getEhcache().getMaxElementsOnDisk());
        bean.setMemoryStoreEvictionPolicy(casProperties.getEhcache().getMemoryStoreEvictionPolicy());
        bean.setOverflowToDisk(casProperties.getEhcache().isOverflowToDisk());
        
        return bean;
    }

    @RefreshScope
    @Bean
    public TicketRegistry ehcacheTicketRegistry() {
        return new EhCacheTicketRegistry();
    }
}
