package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.ssl.SslContextFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.ignite.IgniteProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.registry.IgniteTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CoreTicketUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This is {@link IgniteTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("igniteTicketRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class IgniteTicketRegistryConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    /**
     * Ignite configuration ignite configuration.
     *
     * @param ticketCatalog the ticket catalog
     * @return the ignite configuration
     */
    @Autowired
    @RefreshScope
    @Bean
    public IgniteConfiguration igniteConfiguration(@Qualifier("ticketCatalog") final TicketCatalog ticketCatalog) {
        final var ignite = casProperties.getTicket().getRegistry().getIgnite();

        final var config = new IgniteConfiguration();
        final var spi = new TcpDiscoverySpi();

        if (!StringUtils.isEmpty(ignite.getLocalAddress())) {
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

        final var finder = new TcpDiscoveryVmIpFinder();
        finder.setAddresses(ignite.getIgniteAddress());
        spi.setIpFinder(finder);
        config.setDiscoverySpi(spi);
        final var cacheConfigurations = buildIgniteTicketCaches(ignite, ticketCatalog);
        config.setCacheConfiguration(cacheConfigurations.toArray(new CacheConfiguration[]{}));
        config.setClientMode(ignite.isClientMode());

        final var factory = buildSecureTransportForIgniteConfiguration();
        if (factory != null) {
            config.setSslContextFactory(factory);
        }

        final var dataStorageConfiguration = new DataStorageConfiguration();
        final var dataRegionConfiguration = new DataRegionConfiguration();
        dataRegionConfiguration.setName("DefaultRegion");
        dataRegionConfiguration.setMaxSize(ignite.getDefaultRegionMaxSize());
        dataRegionConfiguration.setPersistenceEnabled(ignite.isDefaultPersistenceEnabled());
        dataStorageConfiguration.setDefaultDataRegionConfiguration(dataRegionConfiguration);

        dataStorageConfiguration.setSystemRegionMaxSize(ignite.getDefaultRegionMaxSize());
        config.setDataStorageConfiguration(dataStorageConfiguration);
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("igniteConfiguration.cacheConfiguration=[{}]", (Object[]) config.getCacheConfiguration());
            LOGGER.debug("igniteConfiguration.getDiscoverySpi=[{}]", config.getDiscoverySpi());
            LOGGER.debug("igniteConfiguration.getSslContextFactory=[{}]", config.getSslContextFactory());
        }

        return config;
    }
    
    @Autowired
    @Bean
    @RefreshScope
    public TicketRegistry ticketRegistry(@Qualifier("ticketCatalog") final TicketCatalog ticketCatalog) {
        final var igniteProperties = casProperties.getTicket().getRegistry().getIgnite();
        final var igniteConfiguration = igniteConfiguration(ticketCatalog);
        final var r = new IgniteTicketRegistry(ticketCatalog, igniteConfiguration, igniteProperties);
        r.setCipherExecutor(CoreTicketUtils.newTicketRegistryCipherExecutor(igniteProperties.getCrypto(), "ignite"));
        r.initialize();
        return r;
    }

    private static Collection<CacheConfiguration> buildIgniteTicketCaches(final IgniteProperties ignite,
                                                                          final TicketCatalog ticketCatalog) {
        final var definitions = ticketCatalog.findAll();
        return definitions
            .stream()
            .map(t -> {
                final var ticketsCache = new CacheConfiguration();
                ticketsCache.setName(t.getProperties().getStorageName());
                ticketsCache.setCacheMode(CacheMode.valueOf(ignite.getTicketsCache().getCacheMode()));
                ticketsCache.setAtomicityMode(CacheAtomicityMode.valueOf(ignite.getTicketsCache().getAtomicityMode()));
                final var writeSync =
                    CacheWriteSynchronizationMode.valueOf(ignite.getTicketsCache().getWriteSynchronizationMode());
                ticketsCache.setWriteSynchronizationMode(writeSync);
                final var duration = new Duration(TimeUnit.SECONDS, t.getProperties().getStorageTimeout());
                ticketsCache.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(duration));
                return ticketsCache;
            })
            .collect(Collectors.toSet());
    }

    private SslContextFactory buildSecureTransportForIgniteConfiguration() {
        final var properties = casProperties.getTicket().getRegistry().getIgnite();
        final var nullKey = "NULL";
        if (StringUtils.hasText(properties.getKeyStoreFilePath()) && StringUtils.hasText(properties.getKeyStorePassword())
            && StringUtils.hasText(properties.getTrustStoreFilePath()) && StringUtils.hasText(properties.getTrustStorePassword())) {
            final var sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStoreFilePath(properties.getKeyStoreFilePath());
            sslContextFactory.setKeyStorePassword(properties.getKeyStorePassword().toCharArray());
            if (nullKey.equals(properties.getTrustStoreFilePath()) && nullKey.equals(properties.getTrustStorePassword())) {
                sslContextFactory.setTrustManagers(SslContextFactory.getDisabledTrustManager());
            } else {
                sslContextFactory.setTrustStoreFilePath(properties.getTrustStoreFilePath());
                sslContextFactory.setTrustStorePassword(properties.getKeyStorePassword().toCharArray());
            }
            if (org.apache.commons.lang3.StringUtils.isNotBlank(properties.getKeyAlgorithm())) {
                sslContextFactory.setKeyAlgorithm(properties.getKeyAlgorithm());
            }
            if (org.apache.commons.lang3.StringUtils.isNotBlank(properties.getProtocol())) {
                sslContextFactory.setProtocol(properties.getProtocol());
            }
            if (org.apache.commons.lang3.StringUtils.isNotBlank(properties.getTrustStoreType())) {
                sslContextFactory.setTrustStoreType(properties.getTrustStoreType());
            }
            if (org.apache.commons.lang3.StringUtils.isNotBlank(properties.getKeyStoreType())) {
                sslContextFactory.setKeyStoreType(properties.getKeyStoreType());
            }
            return sslContextFactory;
        }
        return null;
    }

}
