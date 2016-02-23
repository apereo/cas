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

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link IgniteTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Configuration("igniteConfiguration")
public class IgniteTicketRegistryConfiguration {

    /**
     * The Ignite addresses.
     */
    @Value("${ignite.adresses:localhost:47500}")
    private String igniteAddresses;

    /**
     * The St cache name.
     */
    @Value("${ignite.servicesCache.name:serviceTicketsCache}")
    private String stCacheName;

    /**
     * The St cache mode.
     */
    @Value("${ignite.servicesCache.cacheMode:REPLICATED}")
    private CacheMode stCacheMode;

    /**
     * The St atomicity mode.
     */
    @Value("${ignite.servicesCache.atomicityMode:TRANSACTIONAL}}")
    private CacheAtomicityMode stAtomicityMode;

    /**
     * The St write synchronization mode.
     */
    @Value("${ignite.servicesCache.writeSynchronizationMode:FULL_SYNC}")
    private CacheWriteSynchronizationMode stWriteSynchronizationMode;

    /**
     * The Tgt cache name.
     */
    @Value("${ignite.ticketsCache.name:serviceTicketsCache}")
    private String tgtCacheName;

    /**
     * The Tgt cache mode.
     */
    @Value("${ignite.ticketsCache.cacheMode:REPLICATED}")
    private CacheMode tgtCacheMode;

    /**
     * The Tgt atomicity mode.
     */
    @Value("${ignite.ticketsCache.atomicityMode:TRANSACTIONAL}}")
    private CacheAtomicityMode tgtAtomicityMode;

    /**
     * The Tgt write synchronization mode.
     */
    @Value("${ignite.ticketsCache.writeSynchronizationMode:FULL_SYNC}")
    private CacheWriteSynchronizationMode tgtWriteSynchronizationMode;

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
        final CacheConfiguration servicesCache = new CacheConfiguration();
        servicesCache.setName(this.stCacheName);
        servicesCache.setCacheMode(this.stCacheMode);
        servicesCache.setAtomicityMode(this.stAtomicityMode);
        servicesCache.setWriteSynchronizationMode(this.stWriteSynchronizationMode);

        final CacheConfiguration ticketsCache = new CacheConfiguration();
        ticketsCache.setName(this.tgtCacheName);
        ticketsCache.setCacheMode(this.tgtCacheMode);
        ticketsCache.setAtomicityMode(this.tgtAtomicityMode);
        ticketsCache.setWriteSynchronizationMode(this.tgtWriteSynchronizationMode);

        configurations.add(servicesCache);
        configurations.add(ticketsCache);

        config.setCacheConfiguration(configurations.toArray(new CacheConfiguration[]{}));
        
        return config;
    }
}
