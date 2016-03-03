package org.jasig.cas.config;

import com.google.common.collect.ImmutableSet;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.distribution.RMIBootstrapCacheLoader;
import net.sf.ehcache.distribution.RMISynchronousCacheReplicator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * This is {@link EhcacheTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Configuration("ehcacheTicketRegistryConfiguration")
public class EhcacheTicketRegistryConfiguration {

    /**
     * The Loader async.
     */
    @Value("${ehcache.cache.loader.async:true}")
    private boolean loaderAsync;

    /**
     * The Max chunk size.
     */
    @Value("${ehcache.cache.loader.chunksize:5000000}")
    private int maxChunkSize;

    /**
     * The Maximum batch size.
     */
    @Value("${ehcache.repl.async.batch.size:100}")
    private int maximumBatchSize;

    /**
     * The Replication interval.
     */
    @Value("${ehcache.repl.async.interval:10000}")
    private int replicationInterval;

    /**
     * The Replicate puts.
     */
    @Value("${ehcache.repl.sync.puts:true}")
    private boolean replicatePuts;

    /**
     * The Replicate updates via copy.
     */
    @Value("${ehcache.repl.sync.updatescopy:true}")
    private boolean replicateUpdatesViaCopy;

    /**
     * The Replicate removals.
     */
    @Value("${ehcache.repl.sync.removals:true}")
    private boolean replicateRemovals;

    /**
     * The Replicate updates.
     */
    @Value("${ehcache.repl.sync.updates:true}")
    private boolean replicateUpdates;

    /**
     * The Replicate puts via copy.
     */
    @Value("${ehcache.repl.sync.putscopy:true}")
    private boolean replicatePutsViaCopy;

    /**
     * The Config location.
     */
    @Value("${ehcache.config.file:classpath:ehcache-replicated.xml}")
    private Resource configLocation;

    /**
     * The Shared.
     */
    @Value("${ehcache.cachemanager.shared:false}")
    private boolean shared;

    /**
     * The Cache manager name.
     */
    @Value("${ehcache.cachemanager.name:ticketRegistryCacheManager}")
    private String cacheManagerName;

    /**
     * The Service tickets cache name.
     */
    @Value("${ehcache.cache.name:org.jasig.cas.ticket.TicketCache}")
    private String cacheName;
    
    /**
     * The Disk expiry thread interval seconds.
     */
    @Value("${ehcache.disk.expiry.interval.seconds:0}")
    private int diskExpiryThreadIntervalSeconds;

    /**
     * The Disk persistent.
     */
    @Value("${ehcache.disk.persistent:false}")
    private boolean diskPersistent;

    /**
     * The Eternal.
     */
    @Value("${ehcache.eternal:false}")
    private boolean eternal;

    /**
     * The Max elements in memory.
     */
    @Value("${ehcache.max.elements.memory:10000}")
    private int maxElementsInMemory;

    /**
     * The Max elements on disk.
     */
    @Value("${ehcache.max.elements.disk:0}")
    private int maxElementsOnDisk;

    /**
     * The Memory store eviction policy.
     */
    @Value("${ehcache.eviction.policy:LRU}")
    private String memoryStoreEvictionPolicy;

    /**
     * The Overflow to disk.
     */
    @Value("${ehcache.overflow.disk:false}")
    private boolean overflowToDisk;

    @Value("${ehcache.timeIdle:0}")
    private int cacheTimeToIdle;

    @Value("${ehcache.timeAlive:" + Integer.MAX_VALUE + "}")
    private int cacheTimeToLive;

    /**
     * Ticket rmi synchronous cache replicator rmi synchronous cache replicator.
     *
     * @return the rmi synchronous cache replicator
     */
    @Bean(name = "ticketRMISynchronousCacheReplicator")
    public RMISynchronousCacheReplicator ticketRMISynchronousCacheReplicator() {
        return new RMISynchronousCacheReplicator(this.replicatePuts, this.replicatePutsViaCopy,
                this.replicateUpdates, this.replicateUpdatesViaCopy, this.replicateRemovals);
    }

    /**
     * Ticket cache bootstrap cache loader rmi bootstrap cache loader.
     *
     * @return the rmi bootstrap cache loader
     */
    @Bean(name = "ticketCacheBootstrapCacheLoader")
    public RMIBootstrapCacheLoader ticketCacheBootstrapCacheLoader() {
        return new RMIBootstrapCacheLoader(this.loaderAsync, this.maxChunkSize);
    }


    /**
     * Cache manager eh cache manager factory bean.
     *
     * @return the eh cache manager factory bean
     */
    @Bean(name = "cacheManager")
    public EhCacheManagerFactoryBean cacheManager() {
        final EhCacheManagerFactoryBean bean = new EhCacheManagerFactoryBean();
        bean.setConfigLocation(this.configLocation);
        bean.setShared(this.shared);
        bean.setCacheManagerName(this.cacheManagerName);

        return bean;
    }

    /**
     * Service tickets cache eh cache factory bean.
     *
     * @param manager the manager
     * @return the eh cache factory bean
     */
    @Bean(name = "ehcacheTicketsCache")
    public EhCacheFactoryBean ehcacheTicketsCache(final CacheManager manager) {
        final EhCacheFactoryBean bean = new EhCacheFactoryBean();
        bean.setCacheName(this.cacheName);
        bean.setCacheEventListeners(ImmutableSet.of(ticketRMISynchronousCacheReplicator()));
        bean.setTimeToIdle(this.cacheTimeToIdle);
        bean.setTimeToLive(this.cacheTimeToLive);

        bean.setCacheManager(manager);
        bean.setBootstrapCacheLoader(ticketCacheBootstrapCacheLoader());

        bean.setDiskExpiryThreadIntervalSeconds(this.diskExpiryThreadIntervalSeconds);
        bean.setDiskPersistent(this.diskPersistent);
        bean.setEternal(this.eternal);
        bean.setMaxElementsInMemory(this.maxElementsInMemory);
        bean.setMaxElementsOnDisk(this.maxElementsOnDisk);
        bean.setMemoryStoreEvictionPolicy(this.memoryStoreEvictionPolicy);
        bean.setOverflowToDisk(this.overflowToDisk);
        
        return bean;
    }
    

}
