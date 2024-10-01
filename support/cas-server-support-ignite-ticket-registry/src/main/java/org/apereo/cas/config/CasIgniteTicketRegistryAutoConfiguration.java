package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.support.ignite.IgniteProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketDefinitionProperties;
import org.apereo.cas.ticket.catalog.CasTicketCatalogConfigurationValuesProvider;
import org.apereo.cas.ticket.registry.IgniteTicketDocument;
import org.apereo.cas.ticket.registry.IgniteTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.CoreTicketUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.cache.QueryEntity;
import org.apache.ignite.cache.QueryIndex;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.ssl.SslContextFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.util.StringUtils;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This is {@link CasIgniteTicketRegistryAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.TicketRegistry, module = "ignite")
@AutoConfiguration
public class CasIgniteTicketRegistryAutoConfiguration {

    private static Collection<CacheConfiguration> buildIgniteTicketCaches(final IgniteProperties ignite, final TicketCatalog ticketCatalog) {
        val definitions = ticketCatalog.findAll();
        return definitions
            .stream()
            .map(t -> {
                val ticketsCache = new CacheConfiguration();
                ticketsCache.setName(t.getProperties().getStorageName());
                ticketsCache.setCacheMode(CacheMode.valueOf(ignite.getTicketsCache().getCacheMode()));
                ticketsCache.setAtomicityMode(CacheAtomicityMode.valueOf(ignite.getTicketsCache().getAtomicityMode()));
                val writeSync = CacheWriteSynchronizationMode.valueOf(ignite.getTicketsCache().getWriteSynchronizationMode());
                ticketsCache.setWriteSynchronizationMode(writeSync);
                val duration = new Duration(TimeUnit.SECONDS, t.getProperties().getStorageTimeout());
                ticketsCache.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(duration));
                ticketsCache.setIndexedTypes(String.class, IgniteTicketDocument.class);
                
                val queryEntity = new QueryEntity(String.class, IgniteTicketDocument.class)
                    .setTableName(t.getProperties().getStorageName())
                    .setKeyFields(Set.of("id", "type", "principal", "prefix", "attributes"))
                    .addQueryField("id", String.class.getName(), null)
                    .addQueryField("type", String.class.getName(), null)
                    .addQueryField("principal", String.class.getName(), null)
                    .addQueryField("attributes", Map.class.getName(), null)
                    .addQueryField("prefix", String.class.getName(), null);
                queryEntity.setIndexes(Arrays.asList(new QueryIndex("id"), new QueryIndex("type", false),
                    new QueryIndex("principal", false), new QueryIndex("prefix", false),
                    new QueryIndex("attributes", false)));
                ticketsCache.setQueryEntities(List.of(queryEntity));

                return ticketsCache;
            })
            .collect(Collectors.toSet());
    }

    protected static SslContextFactory buildSecureTransportForIgniteConfiguration(final CasConfigurationProperties casProperties) {
        val properties = casProperties.getTicket().getRegistry().getIgnite();

        if (StringUtils.hasText(properties.getKeyStoreFilePath()) && StringUtils.hasText(properties.getKeyStorePassword())
            && StringUtils.hasText(properties.getTrustStoreFilePath()) && StringUtils.hasText(properties.getTrustStorePassword())) {

            val sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStoreFilePath(properties.getKeyStoreFilePath());
            sslContextFactory.setKeyStorePassword(properties.getKeyStorePassword().toCharArray());
            if (StringUtils.hasText(properties.getTrustStoreFilePath()) && StringUtils.hasText(properties.getTrustStorePassword())) {
                sslContextFactory.setTrustStoreFilePath(properties.getTrustStoreFilePath());
                sslContextFactory.setTrustStorePassword(properties.getTrustStorePassword().toCharArray());
            } else {
                sslContextFactory.setTrustManagers(SslContextFactory.getDisabledTrustManager());
            }
            if (StringUtils.hasText(properties.getKeyAlgorithm())) {
                sslContextFactory.setKeyAlgorithm(properties.getKeyAlgorithm());
            }
            if (StringUtils.hasText(properties.getProtocol())) {
                sslContextFactory.setProtocol(properties.getProtocol());
            }
            if (StringUtils.hasText(properties.getTrustStoreType())) {
                sslContextFactory.setTrustStoreType(properties.getTrustStoreType());
            }
            if (StringUtils.hasText(properties.getKeyStoreType())) {
                sslContextFactory.setKeyStoreType(properties.getKeyStoreType());
            }
            return sslContextFactory;
        }
        return null;
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public IgniteConfiguration igniteConfiguration(
        @Qualifier(TicketCatalog.BEAN_NAME)
        final TicketCatalog ticketCatalog,
        final CasConfigurationProperties casProperties) {
        val ignite = casProperties.getTicket().getRegistry().getIgnite();
        val config = new IgniteConfiguration();
        val spi = new TcpDiscoverySpi();
        if (StringUtils.hasLength(ignite.getLocalAddress())) {
            spi.setLocalAddress(ignite.getLocalAddress());
        }
        if (ignite.getLocalPort() != -1) {
            spi.setLocalPort(ignite.getLocalPort());
        }
        spi.setJoinTimeout(Beans.newDuration(ignite.getJoinTimeout()).toMillis());
        spi.setAckTimeout(Beans.newDuration(ignite.getAckTimeout()).toMillis());
        spi.setNetworkTimeout(Beans.newDuration(ignite.getNetworkTimeout()).toMillis());
        spi.setSocketTimeout(Beans.newDuration(ignite.getSocketTimeout()).toMillis());
        spi.setThreadPriority(ignite.getThreadPriority());
        spi.setForceServerMode(ignite.isForceServerMode());
        val finder = new TcpDiscoveryVmIpFinder();
        finder.setAddresses(ignite.getIgniteAddress());
        spi.setIpFinder(finder);
        config.setDiscoverySpi(spi);
        val cacheConfigurations = buildIgniteTicketCaches(ignite, ticketCatalog);
        config.setCacheConfiguration(cacheConfigurations.toArray(CacheConfiguration[]::new));
        config.setClientMode(ignite.isClientMode());
        val factory = buildSecureTransportForIgniteConfiguration(casProperties);
        if (factory != null) {
            config.setSslContextFactory(factory);
        }
        val dataStorageConfiguration = new DataStorageConfiguration();
        val dataRegionConfiguration = new DataRegionConfiguration();
        dataRegionConfiguration.setName("DefaultRegion");
        dataRegionConfiguration.setMaxSize(ignite.getDefaultRegionMaxSize());
        dataRegionConfiguration.setPersistenceEnabled(ignite.isDefaultPersistenceEnabled());
        dataStorageConfiguration.setDefaultDataRegionConfiguration(dataRegionConfiguration);
        dataStorageConfiguration.setSystemRegionMaxSize(ignite.getDefaultRegionMaxSize());
        config.setDataStorageConfiguration(dataStorageConfiguration);

        val sqlSchemas = ticketCatalog
            .findAll()
            .stream()
            .map(TicketDefinition::getProperties)
            .map(TicketDefinitionProperties::getStorageName)
            .toArray(String[]::new);
        config.getSqlConfiguration().setSqlSchemas(sqlSchemas);
        return config;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public TicketRegistry ticketRegistry(
        @Qualifier(TicketCatalog.BEAN_NAME)
        final TicketCatalog ticketCatalog,
        @Qualifier(TicketSerializationManager.BEAN_NAME)
        final TicketSerializationManager ticketSerializationManager,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("igniteConfiguration")
        final IgniteConfiguration igniteConfiguration) {
        val igniteProperties = casProperties.getTicket().getRegistry().getIgnite();
        val cipher = CoreTicketUtils.newTicketRegistryCipherExecutor(igniteProperties.getCrypto(), "ignite");
        val registry = new IgniteTicketRegistry(cipher, ticketSerializationManager,
            ticketCatalog, applicationContext, igniteConfiguration, igniteProperties);
        registry.initialize();
        return registry;
    }

    @ConditionalOnMissingBean(name = "igniteTicketCatalogConfigurationValuesProvider")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasTicketCatalogConfigurationValuesProvider igniteTicketCatalogConfigurationValuesProvider() {
        return new CasTicketCatalogConfigurationValuesProvider() {
        };
    }
}
