package org.apereo.cas.hz;

import org.apereo.cas.configuration.model.support.hazelcast.BaseHazelcastProperties;
import org.apereo.cas.configuration.model.support.hazelcast.HazelcastClusterProperties;
import org.apereo.cas.util.CollectionUtils;

import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryConfig;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.ManagementCenterConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.config.MergePolicyConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.PartitionGroupConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.config.WANQueueFullBehavior;
import com.hazelcast.config.WanAcknowledgeType;
import com.hazelcast.config.WanPublisherConfig;
import com.hazelcast.config.WanReplicationConfig;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.UUID;

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
        val cluster = hz.getCluster();
        val evictionPolicy = EvictionPolicy.valueOf(cluster.getEvictionPolicy());

        LOGGER.trace("Creating Hazelcast map configuration for [{}] with idle timeoutSeconds [{}] second(s)", mapName, timeoutSeconds);
        val maxSizeConfig = new MaxSizeConfig()
            .setMaxSizePolicy(MaxSizeConfig.MaxSizePolicy.valueOf(cluster.getMaxSizePolicy()))
            .setSize(cluster.getMaxHeapSizePercentage());

        val mergePolicyConfig = new MergePolicyConfig();
        if (StringUtils.hasText(cluster.getMapMergePolicy())) {
            mergePolicyConfig.setPolicy(cluster.getMapMergePolicy());
        }

        return new MapConfig()
            .setName(mapName)
            .setMergePolicyConfig(mergePolicyConfig)
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
    public static Config build(final BaseHazelcastProperties hz, final Map<String, MapConfig> mapConfigs) {
        val cfg = build(hz);
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
    public static Config build(final BaseHazelcastProperties hz, final MapConfig mapConfig) {
        val cfg = new HashMap<String, MapConfig>();
        cfg.put(mapConfig.getName(), mapConfig);
        return build(hz, cfg);
    }

    /**
     * Build config.
     *
     * @param hz the hz
     * @return the config
     */
    public static Config build(final BaseHazelcastProperties hz) {
        val cluster = hz.getCluster();
        val config = new Config();

        config.setLicenseKey(hz.getLicenseKey());

        if (hz.getManagementCenter().isEnabled()) {
            buildManagementCenterConfig(hz, config);
        }

        val networkConfig = new NetworkConfig()
            .setPort(cluster.getPort())
            .setPortAutoIncrement(cluster.isPortAutoIncrement());

        if (StringUtils.hasText(cluster.getLocalAddress())) {
            config.setProperty(BaseHazelcastProperties.HAZELCAST_LOCAL_ADDRESS_PROP, cluster.getLocalAddress());
        }
        if (StringUtils.hasText(cluster.getPublicAddress())) {
            config.setProperty(BaseHazelcastProperties.HAZELCAST_PUBLIC_ADDRESS_PROP, cluster.getPublicAddress());
            networkConfig.setPublicAddress(cluster.getPublicAddress());
        }

        if (cluster.getWanReplication().isEnabled()) {
            if (!StringUtils.hasText(hz.getLicenseKey())) {
                throw new IllegalArgumentException("Cannot activate WAN replication, a Hazelcast enterprise feature, without a license key");
            }
            LOGGER.warn("Using Hazelcast WAN Replication requires a Hazelcast Enterprise subscription. Make sure you "
                + "have acquired the proper license, SDK and tooling from Hazelcast before activating this feature.");
            buildWanReplicationSettingsForConfig(hz, config);
        }

        val joinConfig = cluster.getDiscovery().isEnabled()
            ? createDiscoveryJoinConfig(config, cluster, networkConfig)
            : createDefaultJoinConfig(config, cluster);
        LOGGER.trace("Created Hazelcast join configuration [{}]", joinConfig);
        networkConfig.setJoin(joinConfig);

        LOGGER.trace("Created Hazelcast network configuration [{}]", networkConfig);
        config.setNetworkConfig(networkConfig);

        val instanceName = StringUtils.hasText(cluster.getInstanceName())
            ? cluster.getInstanceName()
            : UUID.randomUUID().toString();
        LOGGER.trace("Configuring Hazelcast instance name [{}]", instanceName);
        return config.setInstanceName(instanceName)
            .setProperty(BaseHazelcastProperties.HAZELCAST_DISCOVERY_ENABLED_PROP, BooleanUtils.toStringTrueFalse(cluster.getDiscovery().isEnabled()))
            .setProperty(BaseHazelcastProperties.IPV4_STACK_PROP, String.valueOf(cluster.isIpv4Enabled()))
            .setProperty(BaseHazelcastProperties.LOGGING_TYPE_PROP, cluster.getLoggingType())
            .setProperty(BaseHazelcastProperties.MAX_HEARTBEAT_SECONDS_PROP, String.valueOf(cluster.getMaxNoHeartbeatSeconds()));
    }

    private static void buildManagementCenterConfig(final BaseHazelcastProperties hz, final Config config) {
        val managementCenter = new ManagementCenterConfig();
        val center = hz.getManagementCenter();
        managementCenter.setEnabled(center.isEnabled());
        managementCenter.setUrl(center.getUrl());
        managementCenter.setUpdateInterval(center.getUpdateInterval());
        config.setManagementCenterConfig(managementCenter);
    }

    private static void buildWanReplicationSettingsForConfig(final BaseHazelcastProperties hz, final Config config) {
        val wan = hz.getCluster().getWanReplication();

        val wanReplicationConfig = new WanReplicationConfig();
        wanReplicationConfig.setName(wan.getReplicationName());

        wan.getTargets().forEach(target -> {
            val nextCluster = new WanPublisherConfig();
            nextCluster.setClassName(target.getPublisherClassName());
            nextCluster.setGroupName(target.getGroupName());
            nextCluster.setQueueFullBehavior(WANQueueFullBehavior.valueOf(target.getQueueFullBehavior()));
            nextCluster.setQueueCapacity(target.getQueueCapacity());

            val props = nextCluster.getProperties();
            props.put("batch.size", target.getBatchSize());
            props.put("batch.max.delay.millis", target.getBatchMaximumDelayMilliseconds());
            props.put("response.timeout.millis", target.getResponseTimeoutMilliseconds());
            props.put("snapshot.enabled", target.isSnapshotEnabled());
            props.put("endpoints", target.getEndpoints());
            if (StringUtils.hasText(target.getGroupPassword())) {
                props.put("group.password", target.getGroupPassword());
            }
            props.put("ack.type", WanAcknowledgeType.valueOf(target.getAcknowledgeType()));
            props.put("executorThreadCount", target.getExecutorThreadCount());
            wanReplicationConfig.addWanPublisherConfig(nextCluster);
        });
        config.addWanReplicationConfig(wanReplicationConfig);
    }

    private static JoinConfig createDiscoveryJoinConfig(final Config config, final HazelcastClusterProperties cluster, final NetworkConfig networkConfig) {
        val joinConfig = new JoinConfig();

        LOGGER.trace("Disabling multicast and TCP/IP configuration for discovery");
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getTcpIpConfig().setEnabled(false);

        val discoveryConfig = new DiscoveryConfig();
        val strategyConfig = locateDiscoveryStrategyConfig(cluster, joinConfig, config, networkConfig);
        LOGGER.trace("Creating discovery strategy configuration as [{}]", strategyConfig);
        discoveryConfig.setDiscoveryStrategyConfigs(CollectionUtils.wrap(strategyConfig));
        joinConfig.setDiscoveryConfig(discoveryConfig);
        return joinConfig;
    }

    private static DiscoveryStrategyConfig locateDiscoveryStrategyConfig(final HazelcastClusterProperties cluster,
                                                                         final JoinConfig joinConfig,
                                                                         final Config config,
                                                                         final NetworkConfig networkConfig) {
        val serviceLoader = ServiceLoader.load(HazelcastDiscoveryStrategy.class);
        val it = serviceLoader.iterator();
        if (it.hasNext()) {
            val strategy = it.next();
            return strategy.get(cluster, joinConfig, config, networkConfig);
        }
        throw new IllegalArgumentException("Could not create discovery strategy configuration. No discovery provider is defined");
    }

    private static JoinConfig createDefaultJoinConfig(final Config config, final HazelcastClusterProperties cluster) {
        val tcpIpConfig = new TcpIpConfig()
            .setEnabled(cluster.isTcpipEnabled())
            .setMembers(cluster.getMembers())
            .setConnectionTimeoutSeconds(cluster.getTimeout());
        LOGGER.trace("Created Hazelcast TCP/IP configuration [{}] for members [{}]", tcpIpConfig, cluster.getMembers());

        val multicastConfig = new MulticastConfig().setEnabled(cluster.isMulticastEnabled());
        if (cluster.isMulticastEnabled()) {
            LOGGER.debug("Created Hazelcast Multicast configuration [{}]", multicastConfig);
            multicastConfig.setMulticastGroup(cluster.getMulticastGroup());
            multicastConfig.setMulticastPort(cluster.getMulticastPort());

            val trustedInterfaces = StringUtils.commaDelimitedListToSet(cluster.getMulticastTrustedInterfaces());
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

    private static Config finalizeConfig(final Config config, final BaseHazelcastProperties hz) {
        if (StringUtils.hasText(hz.getCluster().getPartitionMemberGroupType())) {
            val partitionGroupConfig = config.getPartitionGroupConfig();
            val type = PartitionGroupConfig.MemberGroupType.valueOf(
                hz.getCluster().getPartitionMemberGroupType().toUpperCase());
            LOGGER.trace("Using partition member group type [{}]", type);
            partitionGroupConfig.setEnabled(true).setGroupType(type);
        }
        return config;
    }
}
