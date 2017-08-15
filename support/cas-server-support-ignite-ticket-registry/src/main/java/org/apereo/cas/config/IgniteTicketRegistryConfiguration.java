package org.apereo.cas.config;

import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.ignite.IgniteProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.registry.IgniteTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

/**
 * This is {@link IgniteTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("igniteTicketRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class IgniteTicketRegistryConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    /**
     * Ignite configuration ignite configuration.
     *
     * @param ticketCatalog the ticket catalog
     * @return the ignite configuration
     */
    @RefreshScope
    @Bean
    public IgniteConfiguration igniteConfiguration(@Qualifier("ticketCatalog") final TicketCatalog ticketCatalog) {
        final IgniteProperties ignite = casProperties.getTicket().getRegistry().getIgnite();

        final IgniteConfiguration config = new IgniteConfiguration();
        final TcpDiscoverySpi spi = new TcpDiscoverySpi();

        if (!StringUtils.isEmpty(ignite.getLocalAddress())) {
            spi.setLocalAddress(ignite.getLocalAddress());
        }
        if (ignite.getLocalPort() != -1) {
            spi.setLocalPort(ignite.getLocalPort());
        }
        spi.setJoinTimeout(ignite.getJoinTimeout());
        spi.setAckTimeout(ignite.getAckTimeout());
        spi.setNetworkTimeout(ignite.getNetworkTimeout());
        spi.setSocketTimeout(ignite.getSocketTimeout());
        spi.setThreadPriority(ignite.getThreadPriority());
        spi.setForceServerMode(ignite.isForceServerMode());

        final TcpDiscoveryVmIpFinder finder = new TcpDiscoveryVmIpFinder();
        finder.setAddresses(ignite.getIgniteAddress());
        spi.setIpFinder(finder);
        config.setDiscoverySpi(spi);
        final Collection<CacheConfiguration> cacheConfigurations = buildIgniteTicketCaches(ignite, ticketCatalog);
        config.setCacheConfiguration(cacheConfigurations.toArray(new CacheConfiguration[]{}));
        config.setClientMode(ignite.isClientMode());

        return config;
    }

    private static Collection<CacheConfiguration> buildIgniteTicketCaches(final IgniteProperties ignite,
                                                                          final TicketCatalog ticketCatalog) {
        final Collection<TicketDefinition> definitions = ticketCatalog.findAll();
        return definitions
                .stream()
                .map(t -> {
                    final CacheConfiguration ticketsCache = new CacheConfiguration();
                    ticketsCache.setName(t.getProperties().getStorageName());
                    ticketsCache.setCacheMode(CacheMode.valueOf(ignite.getTicketsCache().getCacheMode()));
                    ticketsCache.setAtomicityMode(CacheAtomicityMode.valueOf(ignite.getTicketsCache().getAtomicityMode()));
                    final CacheWriteSynchronizationMode writeSync =
                            CacheWriteSynchronizationMode.valueOf(ignite.getTicketsCache().getWriteSynchronizationMode());
                    ticketsCache.setWriteSynchronizationMode(writeSync);
                    final Duration duration = new Duration(TimeUnit.SECONDS, t.getProperties().getStorageTimeout());
                    ticketsCache.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(duration));
                    return ticketsCache;
                })
                .collect(Collectors.toSet());
    }

    @Bean
    @RefreshScope
    public TicketRegistry ticketRegistry(@Qualifier("ticketCatalog") final TicketCatalog ticketCatalog) {
        final IgniteProperties igniteProperties = casProperties.getTicket().getRegistry().getIgnite();
        final IgniteTicketRegistry r = new IgniteTicketRegistry(ticketCatalog, igniteConfiguration(ticketCatalog), igniteProperties);
        r.setCipherExecutor(Beans.newTicketRegistryCipherExecutor(igniteProperties.getCrypto(), "ignite"));
        return r;
    }
}
