package org.apereo.cas.config;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.bootstrap.BootstrapCacheLoader;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.distribution.CacheReplicator;
import net.sf.ehcache.distribution.RMIAsynchronousCacheReplicator;
import net.sf.ehcache.distribution.RMIBootstrapCacheLoader;
import net.sf.ehcache.distribution.RMISynchronousCacheReplicator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.ehcache.EhcacheProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.registry.EhCacheTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

/**
 * This is {@link EhcacheTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("ehcacheTicketRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class EhcacheTicketRegistryConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(EhcacheTicketRegistryConfiguration.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "ticketRMISynchronousCacheReplicator")
    public CacheReplicator ticketRMISynchronousCacheReplicator() {
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
    @ConditionalOnMissingBean(name = "ticketRMIAsynchronousCacheReplicator")
    public CacheReplicator ticketRMIAsynchronousCacheReplicator() {
        final EhcacheProperties cache = casProperties.getTicket().getRegistry().getEhcache();
        return new RMIAsynchronousCacheReplicator(
            cache.isReplicatePuts(),
            cache.isReplicatePutsViaCopy(),
            cache.isReplicateUpdates(),
            cache.isReplicateUpdatesViaCopy(),
            cache.isReplicateRemovals(),
            (int) cache.getReplicationInterval(),
            cache.getMaximumBatchSize());
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "ticketCacheBootstrapCacheLoader")
    public BootstrapCacheLoader ticketCacheBootstrapCacheLoader() {
        final EhcacheProperties cache = casProperties.getTicket().getRegistry().getEhcache();
        return new RMIBootstrapCacheLoader(cache.isLoaderAsync(), cache.getMaxChunkSize());
    }

    @Bean
    public EhCacheManagerFactoryBean ehcacheTicketCacheManager() {
        final EhcacheProperties cache = casProperties.getTicket().getRegistry().getEhcache();
        final EhCacheManagerFactoryBean bean = new EhCacheManagerFactoryBean();

        final boolean configExists = ResourceUtils.doesResourceExist(cache.getConfigLocation());
        if (configExists) {
            bean.setConfigLocation(cache.getConfigLocation());
        } else {
            LOGGER.warn("Ehcache configuration file [{}] cannot be found", cache.getConfigLocation());
        }

        bean.setShared(cache.isShared());
        bean.setCacheManagerName(cache.getCacheManagerName());
        return bean;
    }

    private Ehcache buildCache(final TicketDefinition ticketDefinition) {
        final EhcacheProperties cache = casProperties.getTicket().getRegistry().getEhcache();
        final boolean configExists = ResourceUtils.doesResourceExist(cache.getConfigLocation());

        final EhcacheProperties ehcacheProperties = casProperties.getTicket().getRegistry().getEhcache();
        final EhCacheFactoryBean bean = new EhCacheFactoryBean();

        bean.setCacheName(ticketDefinition.getProperties().getStorageName());
        LOGGER.debug("Constructing Ehcache cache [{}]", bean.getName());

        if (configExists) {
            bean.setCacheEventListeners(CollectionUtils.wrapSet(ticketRMISynchronousCacheReplicator()));
            bean.setBootstrapCacheLoader(ticketCacheBootstrapCacheLoader());
        } else {
            LOGGER.warn("In registering ticket definition [{}], Ehcache configuration file [{}] cannot be found "
                + "so no cache event listeners will be configured to bootstrap. "
                + "The ticket registry will operate in standalone mode", ticketDefinition.getPrefix(), cache.getConfigLocation());
        }

        bean.setTimeToIdle((int) ticketDefinition.getProperties().getStorageTimeout());
        bean.setTimeToLive((int) ticketDefinition.getProperties().getStorageTimeout());
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

        bean.afterPropertiesSet();
        return bean.getObject();
    }

    @Autowired
    @RefreshScope
    @Bean
    public TicketRegistry ticketRegistry(@Qualifier("ehcacheTicketCacheManager") final CacheManager manager,
                                         @Qualifier("ticketCatalog") final TicketCatalog ticketCatalog) {
        final EncryptionRandomizedSigningJwtCryptographyProperties crypto = casProperties.getTicket().getRegistry().getEhcache().getCrypto();

        final Collection<TicketDefinition> definitions = ticketCatalog.findAll();
        definitions.forEach(t -> {
            final Ehcache ehcache = buildCache(t);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Created Ehcache cache [{}] for [{}]", ehcache.getName(), t);


                final CacheConfiguration config = ehcache.getCacheConfiguration();
                LOGGER.debug("TicketCache.maxEntriesLocalHeap=[{}]", config.getMaxEntriesLocalHeap());
                LOGGER.debug("TicketCache.maxEntriesLocalDisk=[{}]", config.getMaxEntriesLocalDisk());
                LOGGER.debug("TicketCache.maxEntriesInCache=[{}]", config.getMaxEntriesInCache());
                LOGGER.debug("TicketCache.persistenceConfiguration=[{}]", config.getPersistenceConfiguration().getStrategy());
                LOGGER.debug("TicketCache.synchronousWrites=[{}]", config.getPersistenceConfiguration().getSynchronousWrites());
                LOGGER.debug("TicketCache.timeToLive=[{}]", config.getTimeToLiveSeconds());
                LOGGER.debug("TicketCache.timeToIdle=[{}]", config.getTimeToIdleSeconds());
                LOGGER.debug("TicketCache.cacheManager=[{}]", ehcache.getCacheManager().getName());
            }
            manager.addDecoratedCacheIfAbsent(ehcache);
        });

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The following caches are available: [{}]", (Object[]) manager.getCacheNames());
        }
        return new EhCacheTicketRegistry(ticketCatalog, manager, Beans.newTicketRegistryCipherExecutor(crypto, "ehcache"));
    }
}
