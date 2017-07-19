package org.apereo.cas.hz;

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
import org.apereo.cas.configuration.model.support.hazelcast.HazelcastProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.net.URL;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link HazelcastConfigurationFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class HazelcastConfigurationFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastConfigurationFactory.class);

    /**
     * Build map config map config.
     *
     * @param hz          the hz
     * @param storageName the storage name
     * @param timeout     the timeout
     * @return the map config
     */
    public MapConfig buildMapConfig(final HazelcastProperties hz,
                               final String storageName,
                               final long timeout) {
        final HazelcastProperties.Cluster cluster = hz.getCluster();
        final EvictionPolicy evictionPolicy = EvictionPolicy.valueOf(cluster.getEvictionPolicy());

        LOGGER.debug("Creating Hazelcast map configuration for [{}] with idle timeout [{}] second(s)",
                storageName, timeout);

        return new MapConfig()
                .setName(storageName)
                .setMaxIdleSeconds((int) timeout)
                .setBackupCount(cluster.getBackupCount())
                .setAsyncBackupCount(cluster.getAsyncBackupCount())
                .setEvictionPolicy(evictionPolicy)
                .setMaxSizeConfig(new MaxSizeConfig()
                        .setMaxSizePolicy(MaxSizeConfig.MaxSizePolicy.valueOf(cluster.getMaxSizePolicy()))
                        .setSize(cluster.getMaxHeapSizePercentage()));

    }

    /**
     * Build config.
     *
     * @param hz         the hz
     * @param mapConfigs the map configs
     * @return the config
     */
    public Config build(final HazelcastProperties hz, final Map<String, MapConfig> mapConfigs) {
        final Config cfg = build(hz);
        cfg.setMapConfigs(mapConfigs);
        return cfg;
    }

    /**
     * Build config.
     *
     * @param hz the hz
     * @return the config
     */
    public Config build(final HazelcastProperties hz) {
        final HazelcastProperties.Cluster cluster = hz.getCluster();

        final Config config;
        if (hz.getConfigLocation() != null && hz.getConfigLocation().exists()) {
            try {
                final URL configUrl = hz.getConfigLocation().getURL();
                LOGGER.debug("Loading Hazelcast configuration from [{}]", configUrl);
                config = new XmlConfigBuilder(hz.getConfigLocation().getInputStream()).build();
                config.setConfigurationUrl(configUrl);
            } catch (final Exception e) {
                throw Throwables.propagate(e);
            }
        } else {
            config = new Config();
            config.setProperty("hazelcast.prefer.ipv4.stack", String.valueOf(cluster.isIpv4Enabled()));

            // TCP config
            final TcpIpConfig tcpIpConfig = new TcpIpConfig()
                    .setEnabled(cluster.isTcpipEnabled())
                    .setMembers(cluster.getMembers())
                    .setConnectionTimeoutSeconds(cluster.getTimeout());
            LOGGER.debug("Created Hazelcast TCP/IP configuration [{}]", tcpIpConfig);

            // Multicast config
            final MulticastConfig multicastConfig = new MulticastConfig().setEnabled(cluster.isMulticastEnabled());
            if (cluster.isMulticastEnabled()) {
                multicastConfig.setMulticastGroup(cluster.getMulticastGroup());
                multicastConfig.setMulticastPort(cluster.getMulticastPort());

                final Set<String> trustedInterfaces = StringUtils.commaDelimitedListToSet(cluster.getMulticastTrustedInterfaces());
                if (!trustedInterfaces.isEmpty()) {
                    multicastConfig.setTrustedInterfaces(trustedInterfaces);
                }
                multicastConfig.setMulticastTimeoutSeconds(cluster.getMulticastTimeout());
                multicastConfig.setMulticastTimeToLive(cluster.getMulticastTimeToLive());
            }

            LOGGER.debug("Created Hazelcast Multicast configuration [{}]", multicastConfig);

            // Join config
            final JoinConfig joinConfig = new JoinConfig()
                    .setMulticastConfig(multicastConfig)
                    .setTcpIpConfig(tcpIpConfig);

            LOGGER.debug("Created Hazelcast join configuration [{}]", joinConfig);

            // Network config
            final NetworkConfig networkConfig = new NetworkConfig()
                    .setPort(cluster.getPort())
                    .setPortAutoIncrement(cluster.isPortAutoIncrement())
                    .setJoin(joinConfig);

            LOGGER.debug("Created Hazelcast network configuration [{}]", networkConfig);
            config.setNetworkConfig(networkConfig);
        }
        // Add additional default config properties regardless of the configuration source
        return config.setInstanceName(cluster.getInstanceName())
                .setProperty(HazelcastProperties.LOGGING_TYPE_PROP, cluster.getLoggingType())
                .setProperty(HazelcastProperties.MAX_HEARTBEAT_SECONDS_PROP, String.valueOf(cluster.getMaxNoHeartbeatSeconds()));
    }

}
