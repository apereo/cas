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
        
        return new RMISynchronousCacheReplicator(casProperties.getEhcacheProperties().isReplicatePuts(),
                casProperties.getEhcacheProperties().isReplicatePutsViaCopy(),
                casProperties.getEhcacheProperties().isReplicateUpdates(),
                casProperties.getEhcacheProperties().isReplicateUpdatesViaCopy(),
                casProperties.getEhcacheProperties().isReplicateRemovals());
    }

    /**
     * Ticket cache bootstrap cache loader rmi bootstrap cache loader.
     *
     * @return the rmi bootstrap cache loader
     */
    @RefreshScope
    @Bean
    public RMIBootstrapCacheLoader ticketCacheBootstrapCacheLoader() {
        return new RMIBootstrapCacheLoader(casProperties.getEhcacheProperties().isLoaderAsync(),
                casProperties.getEhcacheProperties().getMaxChunkSize());
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
        bean.setConfigLocation(ResourceUtils.prepareClasspathResourceIfNeeded(casProperties.getEhcacheProperties().getConfigLocation()));
        bean.setShared(casProperties.getEhcacheProperties().isShared());
        bean.setCacheManagerName(casProperties.getEhcacheProperties().getCacheManagerName());

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
        bean.setCacheName(casProperties.getEhcacheProperties().getCacheName());
        bean.setCacheEventListeners(ImmutableSet.of(ticketRMISynchronousCacheReplicator()));
        bean.setTimeToIdle(casProperties.getEhcacheProperties().getCacheTimeToIdle());
        bean.setTimeToLive(casProperties.getEhcacheProperties().getCacheTimeToLive());

        bean.setCacheManager(manager);
        bean.setBootstrapCacheLoader(ticketCacheBootstrapCacheLoader());

        bean.setDiskExpiryThreadIntervalSeconds(casProperties.getEhcacheProperties().getDiskExpiryThreadIntervalSeconds());
        bean.setDiskPersistent(casProperties.getEhcacheProperties().isDiskPersistent());
        bean.setEternal(casProperties.getEhcacheProperties().isEternal());
        bean.setMaxElementsInMemory(casProperties.getEhcacheProperties().getMaxElementsInMemory());
        bean.setMaxElementsOnDisk(casProperties.getEhcacheProperties().getMaxElementsOnDisk());
        bean.setMemoryStoreEvictionPolicy(casProperties.getEhcacheProperties().getMemoryStoreEvictionPolicy());
        bean.setOverflowToDisk(casProperties.getEhcacheProperties().isOverflowToDisk());
        
        return bean;
    }

    @RefreshScope
    @Bean
    public TicketRegistry ehcacheTicketRegistry() {
        return new EhCacheTicketRegistry();
    }
}
