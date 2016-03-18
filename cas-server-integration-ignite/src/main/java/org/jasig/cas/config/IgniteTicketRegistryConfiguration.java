package org.jasig.cas.config;

import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.springframework.beans.factory.annotation.Value;
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
    
    /**
     * The Ignite addresses.
     */
    @Value("${ignite.adresses:localhost:47500}")
    private String igniteAddresses;
    
    /**
     * The cache name.
     */
    @Value("${ignite.ticketsCache.name:TicketsCache}")
    private String cacheName;

    /**
     * The cache mode.
     */
    @Value("${ignite.ticketsCache.cacheMode:REPLICATED}")
    private CacheMode cacheMode;

    /**
     * The atomicity mode.
     */
    @Value("${ignite.ticketsCache.atomicityMode:TRANSACTIONAL}}")
    private CacheAtomicityMode atomicityMode;

    /**
     * The write synchronization mode.
     */
    @Value("${ignite.ticketsCache.writeSynchronizationMode:FULL_SYNC}")
    private CacheWriteSynchronizationMode writeSynchronizationMode;
    
    @Value("${ignite.ticketsCache.timeout:" + Integer.MAX_VALUE + "}")
    private long timeout;
    
    /**
     * Ignite configuration ignite configuration.
     *
     * @return the ignite configuration
     */
    @Bean(name = "igniteConfiguration")
    private IgniteConfiguration igniteConfiguration() {
        final IgniteConfiguration config = new IgniteConfiguration();
        final TcpDiscoverySpi spi = new TcpDiscoverySpi();
        final TcpDiscoveryVmIpFinder finder = new TcpDiscoveryVmIpFinder();
        finder.setAddresses(StringUtils.commaDelimitedListToSet(this.igniteAddresses));
        spi.setIpFinder(finder);
        config.setDiscoverySpi(spi);

        final List<CacheConfiguration> configurations = new ArrayList<>();

        final CacheConfiguration ticketsCache = new CacheConfiguration();
        ticketsCache.setName(this.cacheName);
        ticketsCache.setCacheMode(this.cacheMode);
        ticketsCache.setAtomicityMode(this.atomicityMode);
        ticketsCache.setWriteSynchronizationMode(this.writeSynchronizationMode);
        ticketsCache.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS, this.timeout)));

        configurations.add(ticketsCache);

        config.setCacheConfiguration(configurations.toArray(new CacheConfiguration[]{}));
        
        return config;
    }
}
