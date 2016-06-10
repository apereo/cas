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
import org.apereo.cas.configuration.CasConfigurationProperties;
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
    private CasConfigurationProperties casProperties;
    
    @Bean
    @RefreshScope
    public TicketRegistry hazelcastTicketRegistry() {

        return new HazelcastTicketRegistry(hazelcast(),
                casProperties.getHazelcast().getMapName(),
                casProperties.getHazelcast().getPageSize());
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
        if (casProperties.getHazelcast().getConfigLocation() != null
                && casProperties.getHazelcast().getConfigLocation().exists()) {

            try {
                final URL configUrl = casProperties.getHazelcast().getConfigLocation().getURL();
                config = new XmlConfigBuilder(casProperties.getHazelcast().getConfigLocation().getInputStream()).build();
                config.setConfigurationUrl(configUrl);
            } catch (final Exception e) {
                throw Throwables.propagate(e);
            }

        } else {
            //No config location, so do a default config programmatically with handful of properties exposed by CAS
            config = new Config();
            //TCP config
            final TcpIpConfig tcpIpConfig = new TcpIpConfig()
                    .setEnabled(casProperties.getHazelcast().getCluster().isTcpipEnabled())
                    .setMembers(casProperties.getHazelcast().getCluster().getMembers());

            //Multicast config
            final MulticastConfig multicastConfig = new MulticastConfig()
                    .setEnabled(casProperties.getHazelcast().getCluster().isMulticastEnabled());

            //Join config
            final JoinConfig joinConfig = new JoinConfig()
                    .setMulticastConfig(multicastConfig)
                    .setTcpIpConfig(tcpIpConfig);

            //Network config
            final NetworkConfig networkConfig = new NetworkConfig()
                    .setPort(casProperties.getHazelcast().getCluster().getPort())
                    .setPortAutoIncrement(casProperties.getHazelcast().getCluster().isPortAutoIncrement())
                    .setJoin(joinConfig);

            //Map config
            final MapConfig mapConfig = new MapConfig().setName(casProperties.getHazelcast().getMapName())
                    .setMaxIdleSeconds(casProperties.getTgt().getMaxTimeToLiveInSeconds())
                    .setEvictionPolicy(EvictionPolicy.valueOf(casProperties.getHazelcast().getCluster().getEvictionPolicy()))
                    .setEvictionPercentage(casProperties.getHazelcast().getCluster().getEvictionPercentage())
                    .setMaxSizeConfig(new MaxSizeConfig()
                            .setMaxSizePolicy(MaxSizeConfig.MaxSizePolicy.valueOf(
                                    casProperties.getHazelcast().getCluster().getMaxSizePolicy()))
                            .setSize(casProperties.getHazelcast().getCluster().getMaxHeapSizePercentage()));

            final Map<String, MapConfig> mapConfigs = new HashMap<>();
            mapConfigs.put(casProperties.getHazelcast().getMapName(), mapConfig);

            //Finally aggregate all those config into the main Config
            config.setMapConfigs(mapConfigs).setNetworkConfig(networkConfig);
        }
        //Add additional default config properties regardless of the configuration source
        return config.setInstanceName(casProperties.getHazelcast().getCluster().getInstanceName())
                .setProperty(casProperties.getHazelcast().LOGGING_TYPE_PROP, casProperties.getHazelcast().getCluster().getLoggingType())
                .setProperty(casProperties.getHazelcast().MAX_HEARTBEAT_SECONDS_PROP,
                        String.valueOf(casProperties.getHazelcast().getCluster().getMaxNoHeartbeatSeconds()));
    }
}
