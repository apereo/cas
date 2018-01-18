package org.apereo.cas.hz;

import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryConfig;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.PartitionGroupConfig;
import com.hazelcast.config.TcpIpConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apereo.cas.configuration.model.support.hazelcast.BaseHazelcastProperties;
import org.apereo.cas.configuration.model.support.hazelcast.HazelcastClusterProperties;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * This is {@link HazelcastConfigurationFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class HazelcastConfigurationFactory {
    
    /**
     * Build map config map config.
     *
     * @param hz             the hz
     * @param mapName        the storage name
     * @param timeoutSeconds the timeoutSeconds
     * @return the map config
     */
    public MapConfig buildMapConfig(final BaseHazelcastProperties hz, final String mapName, final long timeoutSeconds) {
        final HazelcastClusterProperties cluster = hz.getCluster();
        final EvictionPolicy evictionPolicy = EvictionPolicy.valueOf(cluster.getEvictionPolicy());

        LOGGER.debug("Creating Hazelcast map configuration for [{}] with idle timeoutSeconds [{}] second(s)", mapName, timeoutSeconds);

        final MaxSizeConfig maxSizeConfig = new MaxSizeConfig()
            .setMaxSizePolicy(MaxSizeConfig.MaxSizePolicy.valueOf(cluster.getMaxSizePolicy()))
            .setSize(cluster.getMaxHeapSizePercentage());

        return new MapConfig()
            .setName(mapName)
            .setMaxIdleSeconds((int) timeoutSeconds)
            .setBackupCount(cluster.getBackupCount())
            .setAsyncBackupCount(cluster.getAsyncBackupCount())
            .setEvictionPolicy(evictionPolicy)
            .setMaxSizeConfig(maxSizeConfig);
    }

    /**
     * Build config.
     *
     * @param hz         the hz
     * @param mapConfigs the map configs
     * @return the config
     */
    public Config build(final BaseHazelcastProperties hz, final Map<String, MapConfig> mapConfigs) {
        final Config cfg = build(hz);
        cfg.setMapConfigs(mapConfigs);
        return finalizeConfig(cfg, hz);
    }

    /**
     * Build config.
     *
     * @param hz        the hz
     * @param mapConfig the map config
     * @return the config
     */
    public Config build(final BaseHazelcastProperties hz, final MapConfig mapConfig) {
        final Map<String, MapConfig> cfg = new HashMap<>();
        cfg.put(mapConfig.getName(), mapConfig);
        return build(hz, cfg);
    }

    /**
     * Build config.
     *
     * @param hz the hz
     * @return the config
     */
    public Config build(final BaseHazelcastProperties hz) {
        final HazelcastClusterProperties cluster = hz.getCluster();
        final Config config = new Config();

        final JoinConfig joinConfig = cluster.getDiscovery().isEnabled()
            ? createDiscoveryJoinConfig(config, hz.getCluster()) : createDefaultJoinConfig(config, hz.getCluster());

        LOGGER.debug("Created Hazelcast join configuration [{}]", joinConfig);

        final NetworkConfig networkConfig = new NetworkConfig()
            .setPort(cluster.getPort())
            .setPortAutoIncrement(cluster.isPortAutoIncrement())
            .setJoin(joinConfig);

        LOGGER.debug("Created Hazelcast network configuration [{}]", networkConfig);
        config.setNetworkConfig(networkConfig);

        return config.setInstanceName(cluster.getInstanceName())
            .setProperty(BaseHazelcastProperties.HAZELCAST_DISCOVERY_ENABLED, BooleanUtils.toStringTrueFalse(cluster.getDiscovery().isEnabled()))
            .setProperty(BaseHazelcastProperties.IPV4_STACK_PROP, String.valueOf(cluster.isIpv4Enabled()))
            .setProperty(BaseHazelcastProperties.LOGGING_TYPE_PROP, cluster.getLoggingType())
            .setProperty(BaseHazelcastProperties.MAX_HEARTBEAT_SECONDS_PROP, String.valueOf(cluster.getMaxNoHeartbeatSeconds()));
    }

    private JoinConfig createDiscoveryJoinConfig(final Config config, final HazelcastClusterProperties cluster) {
        final JoinConfig joinConfig = new JoinConfig();

        LOGGER.debug("Disabling multicast and TCP/IP configuration for discovery");
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getTcpIpConfig().setEnabled(false);

        final DiscoveryConfig discoveryConfig = new DiscoveryConfig();
        final DiscoveryStrategyConfig strategyConfig = locateDiscoveryStrategyConfig(cluster);
        LOGGER.debug("Creating discovery strategy configuration as [{}]", strategyConfig);
        discoveryConfig.setDiscoveryStrategyConfigs(CollectionUtils.wrap(strategyConfig));
        joinConfig.setDiscoveryConfig(discoveryConfig);
        return joinConfig;
    }

    private DiscoveryStrategyConfig locateDiscoveryStrategyConfig(final HazelcastClusterProperties cluster) {
        final ServiceLoader<HazelcastDiscoveryStrategy> serviceLoader = ServiceLoader.load(HazelcastDiscoveryStrategy.class);
        final Iterator<HazelcastDiscoveryStrategy> it = serviceLoader.iterator();
        if (it.hasNext()) {
            final HazelcastDiscoveryStrategy strategy = it.next();
            return strategy.get(cluster);
        }
        throw new IllegalArgumentException("Could not create discovery strategy configuration. No discovery provider is defined in the settings");
    }

    private JoinConfig createDefaultJoinConfig(final Config config, final HazelcastClusterProperties cluster) {
        final TcpIpConfig tcpIpConfig = new TcpIpConfig()
            .setEnabled(cluster.isTcpipEnabled())
            .setMembers(cluster.getMembers())
            .setConnectionTimeoutSeconds(cluster.getTimeout());
        LOGGER.debug("Created Hazelcast TCP/IP configuration [{}] for members [{}]", tcpIpConfig, cluster.getMembers());

        final MulticastConfig multicastConfig = new MulticastConfig().setEnabled(cluster.isMulticastEnabled());
        if (cluster.isMulticastEnabled()) {
            LOGGER.debug("Created Hazelcast Multicast configuration [{}]", multicastConfig);
            multicastConfig.setMulticastGroup(cluster.getMulticastGroup());
            multicastConfig.setMulticastPort(cluster.getMulticastPort());

            final Set<String> trustedInterfaces = StringUtils.commaDelimitedListToSet(cluster.getMulticastTrustedInterfaces());
            if (!trustedInterfaces.isEmpty()) {
                multicastConfig.setTrustedInterfaces(trustedInterfaces);
            }
            multicastConfig.setMulticastTimeoutSeconds(cluster.getMulticastTimeout());
            multicastConfig.setMulticastTimeToLive(cluster.getMulticastTimeToLive());
        } else {
            LOGGER.debug("Skipped Hazelcast Multicast configuration since feature is disabled");
        }

        return new JoinConfig()
            .setMulticastConfig(multicastConfig)
            .setTcpIpConfig(tcpIpConfig);
    }

    private Config finalizeConfig(final Config config, final BaseHazelcastProperties hz) {
        if (StringUtils.hasText(hz.getCluster().getPartitionMemberGroupType())) {
            final PartitionGroupConfig partitionGroupConfig = config.getPartitionGroupConfig();
            final PartitionGroupConfig.MemberGroupType type = PartitionGroupConfig.MemberGroupType.valueOf(
                hz.getCluster().getPartitionMemberGroupType().toUpperCase());
            LOGGER.debug("Using partition member group type [{}]", type);
            partitionGroupConfig.setEnabled(true).setGroupType(type);
        }
        return config;
    }


}
