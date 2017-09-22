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
import org.apereo.cas.ticket.registry.IgniteTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
     * @return the ignite configuration
     */
    @RefreshScope
    @Bean
    public IgniteConfiguration igniteConfiguration() {
        final IgniteProperties ignite = casProperties.getTicket().getRegistry().getIgnite();

        final IgniteConfiguration config = new IgniteConfiguration();
        final TcpDiscoverySpi spi = new TcpDiscoverySpi();

        spi.setHeartbeatFrequency(ignite.getHeartbeatFrequency());
        spi.setJoinTimeout(ignite.getJoinTimeout());

        if (!StringUtils.isEmpty(ignite.getLocalAddress())) {
            spi.setLocalAddress(ignite.getLocalAddress());
        }
        if (ignite.getLocalPort() != -1) {
            spi.setLocalPort(ignite.getLocalPort());
        }
        spi.setNetworkTimeout(ignite.getNetworkTimeout());
        spi.setSocketTimeout(ignite.getSocketTimeout());
        spi.setThreadPriority(ignite.getThreadPriority());
        spi.setForceServerMode(ignite.isForceServerMode());

        final TcpDiscoveryVmIpFinder finder = new TcpDiscoveryVmIpFinder();
        finder.setAddresses(StringUtils.commaDelimitedListToSet(ignite.getIgniteAddresses()));
        spi.setIpFinder(finder);
        config.setDiscoverySpi(spi);

        final List<CacheConfiguration> configurations = new ArrayList<>();

        final CacheConfiguration ticketsCache = new CacheConfiguration();
        ticketsCache.setName(ignite.getTicketsCache().getCacheName());
        ticketsCache.setCacheMode(CacheMode.valueOf(ignite.getTicketsCache().getCacheMode()));
        ticketsCache.setAtomicityMode(CacheAtomicityMode.valueOf(ignite.getTicketsCache().getAtomicityMode()));
        ticketsCache.setWriteSynchronizationMode(
                CacheWriteSynchronizationMode.valueOf(
                        ignite.getTicketsCache().getWriteSynchronizationMode()));
        ticketsCache.setExpiryPolicyFactory(
                CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS,
                        casProperties.getTicket().getTgt().getMaxTimeToLiveInSeconds())));

        configurations.add(ticketsCache);

        config.setCacheConfiguration(configurations.toArray(new CacheConfiguration[]{}));

        return config;
    }

    @Bean
    @RefreshScope
    public TicketRegistry ticketRegistry() {
        final IgniteProperties igniteProperties = casProperties.getTicket().getRegistry().getIgnite();
        final IgniteTicketRegistry r = new IgniteTicketRegistry(igniteConfiguration(), igniteProperties);
        r.setCipherExecutor(Beans.newTicketRegistryCipherExecutor(igniteProperties.getCrypto()));
        return r;
    }
}
