package org.apereo.cas.config;

import com.google.common.base.Throwables;
import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.apereo.cas.configuration.model.core.ticket.TicketGrantingTicketProperties;
import org.apereo.cas.configuration.model.support.hazelcast.HazelcastProperties;
import org.apereo.cas.ticket.registry.HazelcastTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring's Java configuration component for <code>HazelcastInstance</code> that is consumed and used by
 * {@link HazelcastTicketRegistry}.
 * <p>
 * This configuration class has the smarts to choose the configuration source for the {@link HazelcastInstance}
 * that it produces by either loading the native hazelcast XML config file from a resource location indicated by
 * <code>hz.config.location</code> property or if that property is 
 * not set nor a valid location, creates HazelcastInstance programmatically
 * with a handful properties and their defaults (if not set) that it exposes to CAS deployers.
 *
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
@Configuration("hazelcastInstanceConfiguration")
public class HazelcastInstanceConfiguration {

    @Autowired
    private HazelcastProperties hazelcastProperties;

    @Autowired
    private TicketGrantingTicketProperties ticketGrantingTicketProperties;
    
    @Bean
    @RefreshScope
    public TicketRegistry hazelcastTicketRegistry() {
        return new HazelcastTicketRegistry(hazelcast(), 
                hazelcastProperties.getMapName(), hazelcastProperties.getPageSize());
    }
    
    /**
     * Create HazelcastInstance bean.
     *
     * @return HazelcastInstance bean.
     * @throws IOException if parsing of hazelcast xml configuration fails
     */
    @Bean
    public HazelcastInstance hazelcast() {
        return Hazelcast.newHazelcastInstance(getConfig());
    }

    /**
     * Get Hazelcast <code>Config</code> instance.
     *
     * @return Hazelcast Config
     * @throws IOException if parsing of hazelcast xml configuration fails
     */
    private Config getConfig() {
        final Config config;
        if (hazelcastProperties.getConfigLocation() != null 
                && hazelcastProperties.getConfigLocation().exists()) { 
            
            try {
                final URL configUrl = hazelcastProperties.getConfigLocation().getURL();
                config = new XmlConfigBuilder(hazelcastProperties.getConfigLocation().getInputStream()).build();
                config.setConfigurationUrl(configUrl);
            } catch (final Exception e) {
                throw Throwables.propagate(e);
            }
            
        } else {
            //No config location, so do a default config programmatically with handful of properties exposed by CAS
            config = new Config();
            //TCP config
            final TcpIpConfig tcpIpConfig = new TcpIpConfig()
                    .setEnabled(hazelcastProperties.getCluster().isTcpipEnabled())
                    .setMembers(hazelcastProperties.getCluster().getMembers());

            //Multicast config
            final MulticastConfig multicastConfig = new MulticastConfig()
                    .setEnabled(hazelcastProperties.getCluster().isMulticastEnabled());

            //Join config
            final JoinConfig joinConfig = new JoinConfig()
                    .setMulticastConfig(multicastConfig)
                    .setTcpIpConfig(tcpIpConfig);

            //Network config
            final NetworkConfig networkConfig = new NetworkConfig()
                    .setPort(hazelcastProperties.getCluster().getPort())
                    .setPortAutoIncrement(hazelcastProperties.getCluster().isPortAutoIncrement())
                    .setJoin(joinConfig);

            //Map config
            final MapConfig mapConfig = new MapConfig().setName(hazelcastProperties.getMapName())
                    .setMaxIdleSeconds(ticketGrantingTicketProperties.getMaxTimeToLiveInSeconds())
                    .setEvictionPolicy(EvictionPolicy.valueOf(hazelcastProperties.getCluster().getEvictionPolicy()))
                    .setEvictionPercentage(hazelcastProperties.getCluster().getEvictionPercentage())
                    .setMaxSizeConfig(new MaxSizeConfig()
                            .setMaxSizePolicy(MaxSizeConfig.MaxSizePolicy.valueOf(
                                    hazelcastProperties.getCluster().getMaxSizePolicy()))
                            .setSize(hazelcastProperties.getCluster().getMaxHeapSizePercentage()));

            final Map<String, MapConfig> mapConfigs = new HashMap<>();
            mapConfigs.put(hazelcastProperties.getMapName(), mapConfig);

            //Finally aggregate all those config into the main Config
            config.setMapConfigs(mapConfigs).setNetworkConfig(networkConfig);
        }
        //Add additional default config properties regardless of the configuration source
        return config.setInstanceName(hazelcastProperties.getCluster().getInstanceName())
                .setProperty(HazelcastProperties.LOGGING_TYPE_PROP, hazelcastProperties.getCluster().getLoggingType())
                .setProperty(HazelcastProperties.MAX_HEARTBEAT_SECONDS_PROP, 
                        String.valueOf(hazelcastProperties.getCluster().getMaxNoHeartbeatSeconds()));
    }
}
