package org.apereo.cas.config;

import com.google.common.collect.ImmutableSet;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.distribution.RMIBootstrapCacheLoader;
import net.sf.ehcache.distribution.RMISynchronousCacheReplicator;
import org.apereo.cas.configuration.model.support.ehcache.EhcacheProperties;
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
    private EhcacheProperties ehcacheProperties;

    /**
     * Ticket rmi synchronous cache replicator rmi synchronous cache replicator.
     *
     * @return the rmi synchronous cache replicator
     */
    @RefreshScope
    @Bean
    public RMISynchronousCacheReplicator ticketRMISynchronousCacheReplicator() {
        return new RMISynchronousCacheReplicator(ehcacheProperties.isReplicatePuts(),
                ehcacheProperties.isReplicatePutsViaCopy(),
                ehcacheProperties.isReplicateUpdates(),
                ehcacheProperties.isReplicateUpdatesViaCopy(),
                ehcacheProperties.isReplicateRemovals());
    }

    /**
     * Ticket cache bootstrap cache loader rmi bootstrap cache loader.
     *
     * @return the rmi bootstrap cache loader
     */
    @RefreshScope
    @Bean
    public RMIBootstrapCacheLoader ticketCacheBootstrapCacheLoader() {
        return new RMIBootstrapCacheLoader(ehcacheProperties.isLoaderAsync(),
                ehcacheProperties.getMaxChunkSize());
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
        bean.setConfigLocation(ResourceUtils.prepareClasspathResourceIfNeeded(ehcacheProperties.getConfigLocation()));
        bean.setShared(ehcacheProperties.isShared());
        bean.setCacheManagerName(ehcacheProperties.getCacheManagerName());

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
        bean.setCacheName(ehcacheProperties.getCacheName());
        bean.setCacheEventListeners(ImmutableSet.of(ticketRMISynchronousCacheReplicator()));
        bean.setTimeToIdle(ehcacheProperties.getCacheTimeToIdle());
        bean.setTimeToLive(ehcacheProperties.getCacheTimeToLive());

        bean.setCacheManager(manager);
        bean.setBootstrapCacheLoader(ticketCacheBootstrapCacheLoader());

        bean.setDiskExpiryThreadIntervalSeconds(ehcacheProperties.getDiskExpiryThreadIntervalSeconds());
        bean.setDiskPersistent(ehcacheProperties.isDiskPersistent());
        bean.setEternal(ehcacheProperties.isEternal());
        bean.setMaxElementsInMemory(ehcacheProperties.getMaxElementsInMemory());
        bean.setMaxElementsOnDisk(ehcacheProperties.getMaxElementsOnDisk());
        bean.setMemoryStoreEvictionPolicy(ehcacheProperties.getMemoryStoreEvictionPolicy());
        bean.setOverflowToDisk(ehcacheProperties.isOverflowToDisk());
        
        return bean;
    }

    @RefreshScope
    @Bean
    public TicketRegistry ehcacheTicketRegistry() {
        return new EhCacheTicketRegistry();
    }
}
