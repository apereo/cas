package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.registry.EhCache3TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.model.Capacity;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.ehcache.clustered.client.config.builders.ClusteredResourcePoolBuilder;
import org.ehcache.clustered.client.config.builders.ClusteredStoreConfigurationBuilder;
import org.ehcache.clustered.client.config.builders.ClusteringServiceConfigurationBuilder;
import org.ehcache.clustered.client.config.builders.TimeoutsBuilder;
import org.ehcache.clustered.common.Consistency;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.core.config.DefaultConfiguration;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.ehcache.event.EventType;
import org.ehcache.impl.config.persistence.DefaultPersistenceConfiguration;
import org.ehcache.jsr107.Eh107Configuration;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.ehcache.jsr107.config.ConfigurationElementState;
import org.ehcache.jsr107.config.Jsr107Configuration;
import org.ehcache.spi.service.ServiceCreationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.Caching;

import java.io.File;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;

/**
 * This is {@link Ehcache3TicketRegistryConfiguration}.
 *
 * @author Hal Deadman
 * @since 6.2.0
 */
@Configuration(value = "ehcache3TicketRegistryConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.ticket.registry.ehcache3", name = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class Ehcache3TicketRegistryConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @ConditionalOnMissingBean(name = "ehcache3CacheManagerConfiguration")
    public ServiceCreationConfiguration ehcache3CacheManagerConfiguration() {
        val ehcacheProperties = casProperties.getTicket().getRegistry().getEhcache3();
        val terracottaClusterUri = ehcacheProperties.getTerracottaClusterUri();
        if (StringUtils.isNotBlank(terracottaClusterUri)) {
            val resourcePoolCapacity = Capacity.parse(ehcacheProperties.getResourcePoolSize());
            val clusterConfigBuilder = ClusteringServiceConfigurationBuilder.cluster(URI.create(terracottaClusterUri))
                .timeouts(TimeoutsBuilder.timeouts()
                    .connection(Duration.ofSeconds(ehcacheProperties.getClusterConnectionTimeout()))
                    .read(Duration.ofSeconds(ehcacheProperties.getClusterReadWriteTimeout()))
                    .write(Duration.ofSeconds(ehcacheProperties.getClusterReadWriteTimeout())).build())
                .autoCreate(s -> s.defaultServerResource(ehcacheProperties.getDefaultServerResource())
                    .resourcePool(ehcacheProperties.getResourcePoolName(), resourcePoolCapacity.getSize().longValue(),
                        MemoryUnit.valueOf(resourcePoolCapacity.getUnitOfMeasure().name())));
            return clusterConfigBuilder.build();
        }
        val rootDirectory = ehcacheProperties.getRootDirectory();
        val rootDirectoryFile = new File(rootDirectory);
        if (!rootDirectoryFile.exists()) {
            LOGGER.debug("Creating folder for ehcache ticket registry disk cache [{}]", rootDirectory);
            val mkdirResult = rootDirectoryFile.mkdirs();
            if (!mkdirResult) {
                LOGGER.warn("Unable to create folder for ehcache ticket registry disk cache [{}]", rootDirectory);
            }
        }
        return new DefaultPersistenceConfiguration(rootDirectoryFile);
    }

    @Bean
    @ConditionalOnMissingBean(name = "ehcache3TicketCacheManager")
    public javax.cache.CacheManager ehcache3TicketCacheManager(
        @Qualifier("ehcache3CacheManagerConfiguration") final ServiceCreationConfiguration ehcache3CacheManagerConfiguration) {
        val ehcacheProperties = casProperties.getTicket().getRegistry().getEhcache3();
        val ehcacheProvider = (EhcacheCachingProvider) Caching.getCachingProvider(EhcacheCachingProvider.class.getName());
        val statisticsAllEnabled = ehcacheProperties.isEnableStatistics() ? ConfigurationElementState.ENABLED : ConfigurationElementState.DISABLED;
        val managementAllAllEnabled = ehcacheProperties.isEnableManagement() ? ConfigurationElementState.ENABLED : ConfigurationElementState.DISABLED;
        val jsr107Config = new Jsr107Configuration(null, new HashMap<>(),
            false, managementAllAllEnabled, statisticsAllEnabled);
        val configuration = new DefaultConfiguration(ehcacheProvider.getDefaultClassLoader(),
            ehcache3CacheManagerConfiguration,
            jsr107Config);
        return ehcacheProvider.getCacheManager(ehcacheProvider.getDefaultURI(), configuration);
    }

    private CacheConfiguration<String, Ticket> buildCacheConfiguration(final TicketDefinition ticketDefinition) {
        val ehcacheProperties = casProperties.getTicket().getRegistry().getEhcache3();
        val terracottaClusterUri = ehcacheProperties.getTerracottaClusterUri();

        CacheEventListenerConfigurationBuilder cacheEventListenerConfiguration = CacheEventListenerConfigurationBuilder
            .newEventListenerConfiguration(new CasCacheEventListener(),
                EventType.CREATED, EventType.UPDATED, EventType.EXPIRED, EventType.REMOVED, EventType.EVICTED)
            .ordered().asynchronous();

        val storageTimeout = ticketDefinition.getProperties().getStorageTimeout();
        val expiryPolicy = ehcacheProperties.isEternal() ? ExpiryPolicyBuilder.noExpiration()
            : ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(storageTimeout));

        var resourcePools = ResourcePoolsBuilder.heap(ehcacheProperties.getMaxElementsInMemory());
        if (StringUtils.isNotBlank(terracottaClusterUri)) {
            resourcePools = resourcePools.with(
                ClusteredResourcePoolBuilder.clusteredShared(ehcacheProperties.getResourcePoolName()));
        }

        val resourcePoolCapacity = Capacity.parse(ehcacheProperties.getResourcePoolSize());
        resourcePools = resourcePools
            .offheap(resourcePoolCapacity.getSize().longValue(), MemoryUnit.valueOf(resourcePoolCapacity.getUnitOfMeasure().name()));

        if (StringUtils.isBlank(terracottaClusterUri)) {
            val perCacheCapacity = Capacity.parse(ehcacheProperties.getPerCacheSizeOnDisk());
            val persistOnDisk = ehcacheProperties.isPersistOnDisk();
            resourcePools = resourcePools.disk(perCacheCapacity.getSize().longValue(),
                MemoryUnit.valueOf(perCacheCapacity.getUnitOfMeasure().name()), persistOnDisk);
        }

        var cacheConfigBuilder = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            String.class, Ticket.class, resourcePools)
            .withExpiry(expiryPolicy)
            .withService(cacheEventListenerConfiguration);

        if (StringUtils.isNotBlank(terracottaClusterUri)) {
            cacheConfigBuilder = cacheConfigBuilder.withService(
                ClusteredStoreConfigurationBuilder.withConsistency(Consistency.valueOf(ehcacheProperties.getClusteredCacheConsistency().name())));
        }

        return cacheConfigBuilder.build();
    }

    /**
     * Create ticket registry bean with all nececessary caches.
     * Using the spring ehcache wrapper bean so it can be initialized after the caches are built.
     *
     * @param ehcacheManager Spring EhCache manager bean, wraps EhCache manager and is used for cache actuator endpoint.
     * @param ticketCatalog  Ticket Catalog
     * @return Ticket Registry
     */
    @Autowired
    @Bean
    @RefreshScope
    public TicketRegistry ticketRegistry(@Qualifier("ehcache3TicketCacheManager") final javax.cache.CacheManager ehcacheManager,
                                         @Qualifier("ticketCatalog") final TicketCatalog ticketCatalog) {
        val ehcacheProperties = casProperties.getTicket().getRegistry().getEhcache3();
        val crypto = ehcacheProperties.getCrypto();

        val definitions = ticketCatalog.findAll();
        definitions.forEach(t -> {
            val cacheName = t.getProperties().getStorageName();
            if (ehcacheManager.getCache(cacheName, String.class, Ticket.class) == null) {
                val ehcacheConfiguration = buildCacheConfiguration(t);
                ehcacheManager.createCache(cacheName, Eh107Configuration.fromEhcacheCacheConfiguration(ehcacheConfiguration));
            }
        });

        return new EhCache3TicketRegistry(ticketCatalog, ehcacheManager, CoreTicketUtils.newTicketRegistryCipherExecutor(crypto, "ehcache3"));
    }

    /**
     * This bean is used by the spring boot cache actuator which spring boot admin can use to clear caches.
     * Actuator needs to be exposed in order for this bean to be used.
     *
     * @param ehcache3TicketCacheManager JSR107 wrapper of EhCache cache manager to be wrapped by spring cache manager.
     * @return Spring EhCacheCacheManager that wraps EhCache JSR107 CacheManager
     */
    @Bean
    public JCacheCacheManager ehCacheJCacheCacheManager(@Qualifier("ehcache3TicketCacheManager") final javax.cache.CacheManager ehcache3TicketCacheManager) {
        return new JCacheCacheManager(ehcache3TicketCacheManager);
    }

    private static class CasCacheEventListener implements CacheEventListener<String, Ticket> {
        @Override
        public void onEvent(final CacheEvent<? extends String, ? extends Ticket> event) {
            LOGGER.trace("Event Type: {}, Ticket Id: {}", event.getType().name(), event.getKey());
        }
    }
}
