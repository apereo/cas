package org.jasig.cas.ticket.registry.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring's Java configuration component for <code>HazelcastInstance</code> that is consumed and used by
 * <code>HazelcastTicketRegistry</code>.
 *
 * This configuration class has the smarts to choose the configuration source for the <code>HazelcastInstance</code> bean
 * that it produces by either loading the native hazelcast XML config file from a resource location indicated by
 * <i>hz.config.location</i> property or if that property is not set nor a valid location, creates HazelcastInstance programattically
 * with a handful properties and their defaults (if not set) that it exposes to CAS deployers.
 *
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
@Configuration
public class HazelcastInstanceConfiguration {

    @Autowired
    private HazelcastProperties hazelcastProperties;

    /**
     * Create HazelcastInstance bean.
     *
     * @param hazelcastConfigLocation String representation of hazelcast xml config.
     * @param resourceLoader resource loader for loading hazelcast xml configuration resource.
     * @return HazelcastInstance bean.
     *
     * @throws IOException if parsing of hazelcast xml configuration fails
     */
    @Bean
    public HazelcastInstance hazelcast(@Value("${hz.config.location:NO_CONFIG_PROVIDED}") final String hazelcastConfigLocation,
                                       final ResourceLoader resourceLoader) throws IOException {

        final Config hzConfig = getConfig(resourceLoader.getResource(hazelcastConfigLocation));
        return Hazelcast.newHazelcastInstance(hzConfig);
    }

    /**
     * Get Hazelcast <code>Config</code> instance.
     *
     * @param configLocation config location for hazelcast xml
     * @return Hazelcast Config
     *
     * @throws IOException if parsing of hazelcast xml configuration fails
     */
    private Config getConfig(final Resource configLocation) throws IOException {
        final Config config;
        //We have a valid config location for hazelcast xml. Try to parse it and configure Hazelcast instance according to that source
        if (configLocation.exists()) {
            final URL configUrl = configLocation.getURL();
            config = new XmlConfigBuilder(configUrl).build();
            if (ResourceUtils.isFileURL(configUrl)) {
                config.setConfigurationFile(configLocation.getFile());
            } else {
                config.setConfigurationUrl(configUrl);
            }
        } else {
            //No config location, so do a default config programmatically with handful of properties exposed by CAS
            config = new Config();
            //TCP config
            final TcpIpConfig tcpIpConfig = new TcpIpConfig()
                    .setEnabled(this.hazelcastProperties.isTcpipEnabled())
                    .setMembers(this.hazelcastProperties.getMembers());

            //Multicast config
            final MulticastConfig multicastConfig = new MulticastConfig()
                    .setEnabled(this.hazelcastProperties.isMulticastEnabled());

            //Join config
            final JoinConfig joinConfig = new JoinConfig()
                    .setMulticastConfig(multicastConfig)
                    .setTcpIpConfig(tcpIpConfig);

            //Network config
            final NetworkConfig networkConfig = new NetworkConfig()
                    .setPort(this.hazelcastProperties.getPort())
                    .setPortAutoIncrement(this.hazelcastProperties.isPortAutoIncrement())
                    .setJoin(joinConfig);

            //Map config
            final MapConfig mapConfig = new MapConfig().setName(this.hazelcastProperties.getMapName())
                    .setMaxIdleSeconds(this.hazelcastProperties.getMaxIdleSeconds())
                    .setEvictionPolicy(this.hazelcastProperties.getEvictionPolicy())
                    .setEvictionPercentage(this.hazelcastProperties.getEvictionPercentage())
                    .setMaxSizeConfig(new MaxSizeConfig()
                            .setMaxSizePolicy(this.hazelcastProperties.getMaxSizePolicy())
                            .setSize(this.hazelcastProperties.getMaxHeapSizePercentage()));

            final Map<String, MapConfig> mapConfigs = new HashMap<>();
            mapConfigs.put("tickets", mapConfig);

            //Finally aggregate all those config into the main Config
            config.setMapConfigs(mapConfigs).setNetworkConfig(networkConfig);
        }
        //Add additional default config properties regardless of the configuration source
        return config.setInstanceName(hazelcastProperties.getInstanceName())
                .setProperty(HazelcastProperties.LOGGING_TYPE_PROP, this.hazelcastProperties.getLoggingType())
                .setProperty(HazelcastProperties.MAX_HEARTBEAT_SECONDS_PROP, this.hazelcastProperties.getMaxNoHeartbeatSeconds());
    }
}
