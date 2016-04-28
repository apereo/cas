package org.jasig.cas.config;

import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
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
@RefreshScope
@Configuration
@Component("igniteTicketRegistryConfiguration")
@ConfigurationProperties(prefix = "ignite.ticketsCache")
public class IgniteTicketRegistryConfiguration {

    /**
     * The Ignite addresses.
     */
    private String addresses = "localhost:47500";
    
    /**
     * The cache name.
     */
    private String cacheName = "TicketsCache";

    /**
     * The cache mode.
     */
    private CacheMode cacheMode = CacheMode.REPLICATED;

    /**
     * The atomicity mode.
     */
    private CacheAtomicityMode atomicityMode = CacheAtomicityMode.TRANSACTIONAL;

    /**
     * The write synchronization mode.
     */
    private CacheWriteSynchronizationMode writeSynchronizationMode = CacheWriteSynchronizationMode.FULL_SYNC;
    
    private long timeout = Integer.MAX_VALUE;

    public CacheAtomicityMode getAtomicityMode() {
        return atomicityMode;
    }

    public void setAtomicityMode(final CacheAtomicityMode aMode) {
        this.atomicityMode = aMode;
    }

    public CacheMode getCacheMode() {
        return cacheMode;
    }

    public void setCacheMode(final CacheMode cMode) {
        this.cacheMode = cMode;
    }

    public CacheWriteSynchronizationMode getWriteSynchronizationMode() {
        return writeSynchronizationMode;
    }

    public void setWriteSynchronizationMode(final CacheWriteSynchronizationMode wsMode) {
        this.writeSynchronizationMode = wsMode;
    }

    public String getAddresses() {
        return addresses;
    }

    public void setAddresses(final String addresses) {
        this.addresses = addresses;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(final String cacheName) {
        this.cacheName = cacheName;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(final long timeout) {
        this.timeout = timeout;
    }

    /**
     * Ignite configuration ignite configuration.
     *
     * @return the ignite configuration
     */
    @Bean(name = "igniteConfiguration")
    public IgniteConfiguration igniteConfiguration() {
        final IgniteConfiguration config = new IgniteConfiguration();
        final TcpDiscoverySpi spi = new TcpDiscoverySpi();
        final TcpDiscoveryVmIpFinder finder = new TcpDiscoveryVmIpFinder();
        finder.setAddresses(StringUtils.commaDelimitedListToSet(this.addresses));
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
