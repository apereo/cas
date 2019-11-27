package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.ehcache.Ehcache3Properties;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.registry.EhCache3TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.ResourceUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.ehcache.CacheManager;
import org.ehcache.Status;
import org.ehcache.clustered.client.config.builders.ClusteredResourcePoolBuilder;
import org.ehcache.clustered.client.config.builders.ClusteredStoreConfigurationBuilder;
import org.ehcache.clustered.client.config.builders.ClusteringServiceConfigurationBuilder;
import org.ehcache.clustered.common.Consistency;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.CacheManagerConfiguration;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.core.statistics.DefaultStatisticsService;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.ehcache.event.EventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.net.URI;
import java.time.Duration;

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
    @ConditionalOnMissingBean
    public CacheManagerConfiguration<? extends CacheManager> ehcache3CacheManagerConfiguration() {
        val ehcacheProperties = casProperties.getTicket().getRegistry().getEhcache3();

        val terracottaClusterUri = ehcacheProperties.getTerracottaClusterUri();
        if (StringUtils.isNotBlank(terracottaClusterUri)) {
            var clusterConfigBuilder = ClusteringServiceConfigurationBuilder.cluster(URI.create(terracottaClusterUri));
            val connectionMode = ehcacheProperties.getConnectionMode();
            if (Ehcache3Properties.CONNECTION_MODE_AUTOCREATE.equals(connectionMode)) {
                clusterConfigBuilder = clusterConfigBuilder.autoCreate(s ->
                    s.defaultServerResource(ehcacheProperties.getDefaultServerResource())
                     .resourcePool(ehcacheProperties.getResourcePoolName(), ehcacheProperties.getResourcePoolSize(), MemoryUnit.valueOf(ehcacheProperties.getResourcePoolUnits())));
            } else if (Ehcache3Properties.CONNECTION_MODE_CONFIGLESS.equals(connectionMode)) {
                LOGGER.debug("Connecting to terracotta in config-less mode, cluster tier manager must already exist.");
            }
            return clusterConfigBuilder.build();
        }
        val rootDirectory = ehcacheProperties.getRootDirectory();
        if (!ResourceUtils.doesResourceExist(rootDirectory)) {
            LOGGER.debug("Creating folder for ehcache ticket registry disk cache [{}]", rootDirectory);
            val mkdirResult = new File(rootDirectory).mkdirs();
            if (!mkdirResult) {
                LOGGER.warn("Unable to create folder for ehcache ticket registry disk cache [{}]", rootDirectory);
            }
        }
        return CacheManagerBuilder.persistence(rootDirectory);
    }


    @Bean
    @ConditionalOnMissingBean
    public CacheManager ehcache3TicketCacheManager(
        @Qualifier ("ehcache3CacheManagerConfiguration") final CacheManagerConfiguration<? extends CacheManager> cacheManagerConfiguration) {
        var beanBuilder = CacheManagerBuilder.newCacheManagerBuilder().with(cacheManagerConfiguration);
        val statisticsService = new DefaultStatisticsService();
        beanBuilder = beanBuilder.using(statisticsService);
        return beanBuilder.build();
    }

    private CacheConfiguration<String, Ticket> buildCache(final TicketDefinition ticketDefinition) {
        val ehcacheProperties = casProperties.getTicket().getRegistry().getEhcache3();
        val terracottaClusterUri = ehcacheProperties.getTerracottaClusterUri();

        CacheEventListenerConfigurationBuilder cacheEventListenerConfiguration = CacheEventListenerConfigurationBuilder
            .newEventListenerConfiguration(new CasCacheEventListener(),
                EventType.CREATED, EventType.UPDATED, EventType.EXPIRED, EventType.REMOVED, EventType.EVICTED)
            .ordered().asynchronous();

        val storageTimeout = ticketDefinition.getProperties().getStorageTimeout();
        val expiryPolicy = ehcacheProperties.isEternal()
            ?
            ExpiryPolicyBuilder.noExpiration()
            :
            ExpiryPolicyBuilder.expiry()
            .create(Duration.ofSeconds(storageTimeout))
            .access(Duration.ofSeconds(storageTimeout))
            .update(Duration.ofSeconds(storageTimeout)).build();

        var resourcePools = ResourcePoolsBuilder.heap(ehcacheProperties.getMaxElementsInMemory());
        if (StringUtils.isNotBlank(terracottaClusterUri)) {
            resourcePools = resourcePools.with(
                ClusteredResourcePoolBuilder.clusteredShared(ehcacheProperties.getResourcePoolName()));
        }

        resourcePools = resourcePools
            .offheap(ehcacheProperties.getResourcePoolSize(), MemoryUnit.valueOf(ehcacheProperties.getResourcePoolUnits()));

        if (StringUtils.isBlank(terracottaClusterUri)) {
            resourcePools = resourcePools.disk(ehcacheProperties.getPerCacheSizeOnDisk(), MemoryUnit.valueOf(ehcacheProperties.getPerCacheSizeOnDiskUnits()));
        }

        var cacheConfigBuilder = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            String.class, Ticket.class, resourcePools)
            .withExpiry(expiryPolicy)
            .withService(cacheEventListenerConfiguration);

        if (StringUtils.isNotBlank(terracottaClusterUri)) {
            cacheConfigBuilder = cacheConfigBuilder.withService(
                ClusteredStoreConfigurationBuilder.withConsistency(Consistency.STRONG));
        }

        return cacheConfigBuilder.build();
    }

    private static class CasCacheEventListener implements CacheEventListener<String, Ticket> {

        @Override
        public void onEvent(final CacheEvent<? extends String, ? extends Ticket> event) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Event Type: {}, Ticket Id: {}", event.getType().name(), event.getKey());
            }
        }
    }

    /**
     * Create ticket registry bean with all nececessary caches.
     * Using the spring ehcache wrapper bean so it can be initialized after the caches are built.
     * @param ehcacheManager Spring EhCache manager bean, wraps EhCache manager and is used for cache actuator endpoint.
     * @param ticketCatalog Ticket Catalog
     * @return Ticket Registry
     */
    @Autowired
    @Bean
    @RefreshScope
    public TicketRegistry ticketRegistry(@Qualifier("ehcache3TicketCacheManager") final CacheManager ehcacheManager,
                                         @Qualifier("ticketCatalog") final TicketCatalog ticketCatalog) {
        val crypto = casProperties.getTicket().getRegistry().getEhcache().getCrypto();

        if (Status.UNINITIALIZED.equals(ehcacheManager.getStatus())) {
            ehcacheManager.init();
        }

        val definitions = ticketCatalog.findAll();
        definitions.forEach(t -> {
            val ehcacheConfiguration = buildCache(t);
            ehcacheManager.createCache(t.getProperties().getStorageName(), ehcacheConfiguration);
        });

        return new EhCache3TicketRegistry(ticketCatalog, ehcacheManager, CoreTicketUtils.newTicketRegistryCipherExecutor(crypto, "ehcache"));
    }

}
