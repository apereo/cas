package org.apereo.cas.config;

import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apereo.cas.configuration.model.core.ticket.TicketGrantingTicketProperties;
import org.apereo.cas.configuration.model.support.ignite.IgniteProperties;
import org.apereo.cas.ticket.registry.IgniteTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.beans.factory.annotation.Autowired;
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
@Configuration("igniteConfiguration")
public class IgniteTicketRegistryConfiguration {
    
    @Autowired
    private IgniteProperties igniteProperties;

    @Autowired
    private TicketGrantingTicketProperties ticketGrantingTicketProperties;
    
    /**
     * Ignite configuration ignite configuration.
     *
     * @return the ignite configuration
     */
    @RefreshScope
    @Bean
    public IgniteConfiguration igniteConfiguration() {
        final IgniteConfiguration config = new IgniteConfiguration();
        final TcpDiscoverySpi spi = new TcpDiscoverySpi();
        final TcpDiscoveryVmIpFinder finder = new TcpDiscoveryVmIpFinder();
        finder.setAddresses(StringUtils.commaDelimitedListToSet(igniteProperties.getIgniteAddresses()));
        spi.setIpFinder(finder);
        config.setDiscoverySpi(spi);

        final List<CacheConfiguration> configurations = new ArrayList<>();

        final CacheConfiguration ticketsCache = new CacheConfiguration();
        ticketsCache.setName(igniteProperties.getTicketsCache().getCacheName());
        ticketsCache.setCacheMode(CacheMode.valueOf(igniteProperties.getTicketsCache().getCacheMode()));
        ticketsCache.setAtomicityMode(CacheAtomicityMode.valueOf(igniteProperties.getTicketsCache().getAtomicityMode()));
        ticketsCache.setWriteSynchronizationMode(
                CacheWriteSynchronizationMode.valueOf(igniteProperties.getTicketsCache().getWriteSynchronizationMode()));
        ticketsCache.setExpiryPolicyFactory(
                CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS,
                        ticketGrantingTicketProperties.getMaxTimeToLiveInSeconds())));

        configurations.add(ticketsCache);

        config.setCacheConfiguration(configurations.toArray(new CacheConfiguration[]{}));
        
        return config;
    }
    
    @Bean
    @RefreshScope
    public TicketRegistry igniteTicketRegistry() {
        return new IgniteTicketRegistry();
    }
}
