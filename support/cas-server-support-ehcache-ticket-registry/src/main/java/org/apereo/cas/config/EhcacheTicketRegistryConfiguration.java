package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.registry.DefaultTicketRegistry;
import org.apereo.cas.ticket.registry.EhCacheTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.AsciiArtUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.event.EventListener;

import java.util.Arrays;

/**
 * This is {@link EhcacheTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 * @deprecated Since 6.2, due to Ehcache 2.x being unmaintained. Other registries are available, including Ehcache 3.x.
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Deprecated(since = "6.2.0")
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry, module = "ehcache2")
@AutoConfiguration
public class EhcacheTicketRegistryConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.ticket.registry.ehcache.enabled").isTrue().evenIfMissing();

    private static Ehcache buildCache(final CasConfigurationProperties casProperties,
                                      final TicketDefinition ticketDefinition,
                                      final CacheReplicator ticketRMISynchronousCacheReplicator,
                                      final BootstrapCacheLoader ticketCacheBootstrapCacheLoader) {
        val ehcacheProperties = casProperties.getTicket().getRegistry().getEhcache();
        val configExists = ResourceUtils.doesResourceExist(ehcacheProperties.getConfigLocation());

        val bean = new EhCacheFactoryBean();

        bean.setCacheName(ticketDefinition.getProperties().getStorageName());
        LOGGER.debug("Constructing Ehcache cache [{}]", bean.getName());

        if (configExists) {
            bean.setCacheEventListeners(CollectionUtils.wrapSet(ticketRMISynchronousCacheReplicator));
            bean.setBootstrapCacheLoader(ticketCacheBootstrapCacheLoader);
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

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "ticketRMISynchronousCacheReplicator")
    public CacheReplicator ticketRMISynchronousCacheReplicator(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(CacheReplicator.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val cache = casProperties.getTicket().getRegistry().getEhcache();
                return new RMISynchronousCacheReplicator(
                    cache.isReplicatePuts(),
                    cache.isReplicatePutsViaCopy(),
                    cache.isReplicateUpdates(),
                    cache.isReplicateUpdatesViaCopy(),
                    cache.isReplicateRemovals());
            })
            .otherwiseProxy()
            .get();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "ticketRMIAsynchronousCacheReplicator")
    public CacheReplicator ticketRMIAsynchronousCacheReplicator(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(CacheReplicator.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val cache = casProperties.getTicket().getRegistry().getEhcache();
                return new RMIAsynchronousCacheReplicator(
                    cache.isReplicatePuts(),
                    cache.isReplicatePutsViaCopy(),
                    cache.isReplicateUpdates(),
                    cache.isReplicateUpdatesViaCopy(),
                    cache.isReplicateRemovals(),
                    (int) Beans.newDuration(cache.getReplicationInterval()).toMillis(),
                    cache.getMaximumBatchSize());
            })
            .otherwiseProxy()
            .get();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "ticketCacheBootstrapCacheLoader")
    public BootstrapCacheLoader ticketCacheBootstrapCacheLoader(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(BootstrapCacheLoader.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val cache = casProperties.getTicket().getRegistry().getEhcache();
                return new RMIBootstrapCacheLoader(cache.isLoaderAsync(), cache.getMaxChunkSize());
            })
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "ehcacheTicketCacheManager")
    public CacheManager ehcacheTicketCacheManager(
        @Qualifier("ticketRMISynchronousCacheReplicator")
        final CacheReplicator ticketRMISynchronousCacheReplicator,
        @Qualifier("ticketCacheBootstrapCacheLoader")
        final BootstrapCacheLoader ticketCacheBootstrapCacheLoader,
        @Qualifier(TicketCatalog.BEAN_NAME)
        final TicketCatalog ticketCatalog,
        final CasConfigurationProperties casProperties) {
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
        bean.afterPropertiesSet();
        val ehCacheManager = bean.getObject();

        val definitions = ticketCatalog.findAll();
        definitions.forEach(ticketDefn -> {
            val ehcache = buildCache(casProperties, ticketDefn, ticketRMISynchronousCacheReplicator, ticketCacheBootstrapCacheLoader);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Created Ehcache cache [{}] for [{}]", ehcache.getName(), ticketDefn);
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
        LOGGER.debug("The following caches are available: [{}]", Arrays.toString(ehCacheManager.getCacheNames()));
        return ehCacheManager;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TicketRegistry ticketRegistry(
        @Qualifier(TicketCatalog.BEAN_NAME)
        final TicketCatalog ticketCatalog,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("ehcacheTicketCacheManager")
        final CacheManager ehCacheManager) {
        return BeanSupplier.of(TicketRegistry.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val crypto = casProperties.getTicket().getRegistry().getEhcache().getCrypto();
                val registry = new EhCacheTicketRegistry(ticketCatalog, ehCacheManager);
                registry.setCipherExecutor(CoreTicketUtils.newTicketRegistryCipherExecutor(crypto, "ehcache"));
                return registry;
            })
            .otherwise(DefaultTicketRegistry::new)
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AbstractCacheManager ehCacheCacheManager(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("ehcacheTicketCacheManager")
        final CacheManager ehcacheTicketCacheManager) {
        return BeanSupplier.of(AbstractCacheManager.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new EhCacheCacheManager(ehcacheTicketCacheManager))
            .otherwise(SimpleCacheManager::new)
            .get();
    }

    @EventListener
    public void handleApplicationReadyEvent(final ApplicationReadyEvent event) throws Exception {
        val ehCacheCacheManager = event.getApplicationContext().getBean("ehCacheCacheManager", AbstractCacheManager.class);
        ehCacheCacheManager.initializeCaches();
    }
}
