package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.registry.EhCacheTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.AsciiArtUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.ResourceUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.bootstrap.BootstrapCacheLoader;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.distribution.CacheReplicator;
import net.sf.ehcache.distribution.RMIAsynchronousCacheReplicator;
import net.sf.ehcache.distribution.RMIBootstrapCacheLoader;
import net.sf.ehcache.distribution.RMISynchronousCacheReplicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.Objects;

/**
 * This is {@link EhcacheTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 * @deprecated Since 6.2, due to Ehcache 2.x being unmaintained. Other registries are available, including Ehcache 3.x.
 */
@Configuration("ehcacheTicketRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.ticket.registry.ehcache", name = "enabled", havingValue = "true", matchIfMissing = true)
@Deprecated(since = "6.2.0")
@Slf4j
public class EhcacheTicketRegistryConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "ticketRMISynchronousCacheReplicator")
    public CacheReplicator ticketRMISynchronousCacheReplicator() {
        val cache = casProperties.getTicket().getRegistry().getEhcache();
        return new RMISynchronousCacheReplicator(
            cache.isReplicatePuts(),
            cache.isReplicatePutsViaCopy(),
            cache.isReplicateUpdates(),
            cache.isReplicateUpdatesViaCopy(),
            cache.isReplicateRemovals());
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "ticketRMIAsynchronousCacheReplicator")
    public CacheReplicator ticketRMIAsynchronousCacheReplicator() {
        val cache = casProperties.getTicket().getRegistry().getEhcache();
        return new RMIAsynchronousCacheReplicator(
            cache.isReplicatePuts(),
            cache.isReplicatePutsViaCopy(),
            cache.isReplicateUpdates(),
            cache.isReplicateUpdatesViaCopy(),
            cache.isReplicateRemovals(),
            (int) Beans.newDuration(cache.getReplicationInterval()).toMillis(),
            cache.getMaximumBatchSize());
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "ticketCacheBootstrapCacheLoader")
    public BootstrapCacheLoader ticketCacheBootstrapCacheLoader() {
        val cache = casProperties.getTicket().getRegistry().getEhcache();
        return new RMIBootstrapCacheLoader(cache.isLoaderAsync(), cache.getMaxChunkSize());
    }

    @Lazy(false)
    @Bean
    public EhCacheManagerFactoryBean ehcacheTicketCacheManager() {
        AsciiArtUtils.printAsciiArtWarning(LOGGER,
            "CAS Integration with ehcache 2.x will be discontinued after CAS 6.2.x. Consider migrating to another type of registry.");

        val cache = casProperties.getTicket().getRegistry().getEhcache();
        val bean = new EhCacheManagerFactoryBean();

        cache.getSystemProps().forEach(System::setProperty);

        val configExists = ResourceUtils.doesResourceExist(cache.getConfigLocation());
        if (configExists) {
            bean.setConfigLocation(cache.getConfigLocation());
        } else {
            LOGGER.warn("Ehcache configuration file [{}] cannot be found", cache.getConfigLocation());
        }

        bean.setShared(cache.isShared());
        bean.setCacheManagerName(cache.getCacheManagerName());

        return bean;
    }

    /**
     * Create ticket registry bean with all necessary caches.
     * Using the spring ehcache wrapper bean so it can be initialized after the caches are built.
     *
     * @param manager       Spring EhCache manager bean, wraps EhCache manager and is used for cache actuator endpoint.
     * @param ticketCatalog Ticket Catalog
     * @return Ticket Registry
     */
    @Autowired
    @Bean
    @RefreshScope
    @Lazy(false)
    public TicketRegistry ticketRegistry(@Qualifier("ehCacheCacheManager") final EhCacheCacheManager manager,
                                         @Qualifier("ticketCatalog") final TicketCatalog ticketCatalog) {

        val ehCacheManager = Objects.requireNonNull(manager.getCacheManager());
        val crypto = casProperties.getTicket().getRegistry().getEhcache().getCrypto();

        val definitions = ticketCatalog.findAll();
        definitions.forEach(t -> {
            val ehcache = buildCache(t);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Created Ehcache cache [{}] for [{}]", ehcache.getName(), t);
                val config = ehcache.getCacheConfiguration();
                LOGGER.debug("[{}].maxEntriesLocalHeap=[{}]", ehcache.getName(), config.getMaxEntriesLocalHeap());
                LOGGER.debug("[{}].maxEntriesLocalDisk=[{}]", ehcache.getName(), config.getMaxEntriesLocalDisk());
                LOGGER.debug("[{}].maxEntriesInCache=[{}]", ehcache.getName(), config.getMaxEntriesInCache());
                LOGGER.debug("[{}].persistenceConfiguration=[{}]", ehcache.getName(), config.getPersistenceConfiguration().getStrategy());
                LOGGER.debug("[{}].synchronousWrites=[{}]", ehcache.getName(), config.getPersistenceConfiguration().getSynchronousWrites());
                LOGGER.debug("[{}].timeToLive=[{}]", ehcache.getName(), config.getTimeToLiveSeconds());
                LOGGER.debug("[{}].timeToIdle=[{}]", ehcache.getName(), config.getTimeToIdleSeconds());
                LOGGER.debug("[{}].cacheManager=[{}]", ehcache.getName(), ehcache.getCacheManager().getName());
            }
            ehCacheManager.addDecoratedCacheIfAbsent(ehcache);
        });

        manager.initializeCaches();
        LOGGER.debug("The following caches are available: [{}]", manager.getCacheNames());
        return new EhCacheTicketRegistry(ticketCatalog, ehCacheManager,
            CoreTicketUtils.newTicketRegistryCipherExecutor(crypto, "ehcache"));
    }

    /**
     * This bean is used by the spring boot cache actuator which spring boot admin can use to clear caches.
     *
     * @param ehcacheTicketCacheManager EhCache cache manager to be wrapped by spring cache manager.
     * @return Spring {@link EhCacheCacheManager} that wraps EhCache {@link CacheManager}
     */
    @Bean
    @Autowired
    public EhCacheCacheManager ehCacheCacheManager(final CacheManager ehcacheTicketCacheManager) {
        return new EhCacheCacheManager(ehcacheTicketCacheManager);
    }

    private Ehcache buildCache(final TicketDefinition ticketDefinition) {
        val ehcacheProperties = casProperties.getTicket().getRegistry().getEhcache();
        val configExists = ResourceUtils.doesResourceExist(ehcacheProperties.getConfigLocation());

        val bean = new EhCacheFactoryBean();

        bean.setCacheName(ticketDefinition.getProperties().getStorageName());
        LOGGER.debug("Constructing Ehcache cache [{}]", bean.getName());

        if (configExists) {
            bean.setCacheEventListeners(CollectionUtils.wrapSet(ticketRMISynchronousCacheReplicator()));
            bean.setBootstrapCacheLoader(ticketCacheBootstrapCacheLoader());
        } else {
            LOGGER.warn("In registering ticket definition [{}], Ehcache configuration file [{}] cannot be found "
                + "so no cache event listeners will be configured to bootstrap. "
                + "The ticket registry will operate in standalone mode", ticketDefinition.getPrefix(), ehcacheProperties.getConfigLocation());
        }

        bean.setTimeToIdle((int) ticketDefinition.getProperties().getStorageTimeout());
        bean.setTimeToLive((int) ticketDefinition.getProperties().getStorageTimeout());
        bean.setDiskExpiryThreadIntervalSeconds(ehcacheProperties.getDiskExpiryThreadIntervalSeconds());
        bean.setEternal(ehcacheProperties.isEternal());
        bean.setMaxEntriesLocalHeap(ehcacheProperties.getMaxElementsInMemory());
        bean.setMaxEntriesInCache(ehcacheProperties.getMaxElementsInCache());
        bean.setMaxEntriesLocalDisk(ehcacheProperties.getMaxElementsOnDisk());
        bean.setMemoryStoreEvictionPolicy(ehcacheProperties.getMemoryStoreEvictionPolicy());
        val c = new PersistenceConfiguration();
        c.strategy(ehcacheProperties.getPersistence());
        c.setSynchronousWrites(ehcacheProperties.isSynchronousWrites());
        bean.persistence(c);

        bean.afterPropertiesSet();
        return bean.getObject();
    }

}
