package org.apereo.cas.hz;

import org.apereo.cas.configuration.model.support.hazelcast.BaseHazelcastProperties;
import org.apereo.cas.configuration.model.support.hazelcast.HazelcastClusterProperties;
import org.apereo.cas.util.CollectionUtils;

import com.hazelcast.config.Config;
import com.hazelcast.config.ConsistencyCheckStrategy;
import com.hazelcast.config.DiscoveryConfig;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.ManagementCenterConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizePolicy;
import com.hazelcast.config.MergePolicyConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.PartitionGroupConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.config.WanAcknowledgeType;
import com.hazelcast.config.WanBatchPublisherConfig;
import com.hazelcast.config.WanQueueFullBehavior;
import com.hazelcast.config.WanReplicationConfig;
import com.hazelcast.config.WanSyncConfig;
import com.hazelcast.spi.merge.DiscardMergePolicy;
import com.hazelcast.spi.merge.ExpirationTimeMergePolicy;
import com.hazelcast.spi.merge.HigherHitsMergePolicy;
import com.hazelcast.spi.merge.LatestAccessMergePolicy;
import com.hazelcast.spi.merge.LatestUpdateMergePolicy;
import com.hazelcast.spi.merge.PassThroughMergePolicy;
import com.hazelcast.spi.merge.PutIfAbsentMergePolicy;
import com.hazelcast.wan.WanPublisherState;
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

        buildManagementCenterConfig(config);

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

        cluster.getOutboundPorts().forEach(networkConfig::addOutboundPortDefinition);

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
            : createDefaultJoinConfig(cluster);
        LOGGER.trace("Created Hazelcast join configuration [{}]", joinConfig);
        networkConfig.setJoin(joinConfig);

        LOGGER.trace("Created Hazelcast network configuration [{}]", networkConfig);
        config.setNetworkConfig(networkConfig);

        LOGGER.trace("Enables compression: {}", hz.isEnableCompression());
        config.getSerializationConfig().setEnableCompression(hz.isEnableCompression());

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

    private static void buildManagementCenterConfig(final Config config) {
        val managementCenter = new ManagementCenterConfig();
        managementCenter.setScriptingEnabled(true);
        config.setManagementCenterConfig(managementCenter);
    }

    private static void buildWanReplicationSettingsForConfig(final BaseHazelcastProperties hz, final Config config) {
        val wan = hz.getCluster().getWanReplication();

        val wanReplicationConfig = new WanReplicationConfig();
        wanReplicationConfig.setName(wan.getReplicationName());

        wan.getTargets().forEach(target -> {
            val nextCluster = new WanBatchPublisherConfig();
            nextCluster.setClassName(target.getPublisherClassName());
            nextCluster.setQueueFullBehavior(WanQueueFullBehavior.valueOf(target.getQueueFullBehavior()));
            nextCluster.setQueueCapacity(target.getQueueCapacity());
            nextCluster.setAcknowledgeType(WanAcknowledgeType.valueOf(target.getAcknowledgeType()));
            nextCluster.setBatchSize(target.getBatchSize());
            nextCluster.setBatchMaxDelayMillis(target.getBatchMaximumDelayMilliseconds());
            nextCluster.setResponseTimeoutMillis(target.getResponseTimeoutMilliseconds());
            nextCluster.setSnapshotEnabled(target.isSnapshotEnabled());
            nextCluster.setTargetEndpoints(target.getEndpoints());
            nextCluster.setClusterName(target.getClusterName());
            nextCluster.setPublisherId(target.getPublisherId());
            nextCluster.setMaxConcurrentInvocations(target.getExecutorThreadCount());
            nextCluster.setInitialPublisherState(WanPublisherState.REPLICATING);
            nextCluster.setProperties(target.getProperties());
            nextCluster.setSyncConfig(new WanSyncConfig()
                .setConsistencyCheckStrategy(ConsistencyCheckStrategy.valueOf(target.getConsistencyCheckStrategy())));
            wanReplicationConfig.addBatchReplicationPublisherConfig(nextCluster);
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

    private static JoinConfig createDefaultJoinConfig(final HazelcastClusterProperties cluster) {
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

    /**
     * Build map config map config.
     *
     * @param hz             the hz
     * @param mapName        the storage name
     * @param timeoutSeconds the timeoutSeconds
     * @return the map config
     */
    public static MapConfig buildMapConfig(final BaseHazelcastProperties hz, final String mapName, final long timeoutSeconds) {
        val cluster = hz.getCluster();

        val evictionPolicy = EvictionPolicy.valueOf(cluster.getEvictionPolicy());

        val evictionConfig = new EvictionConfig();
        evictionConfig.setEvictionPolicy(evictionPolicy);
        evictionConfig.setMaxSizePolicy(MaxSizePolicy.valueOf(cluster.getMaxSizePolicy()));
        evictionConfig.setSize(cluster.getMaxSize());

        val mergePolicyConfig = new MergePolicyConfig();
        if (StringUtils.hasText(cluster.getMapMergePolicy())) {
            switch (cluster.getMapMergePolicy().trim().toLowerCase()) {
                case "discard":
                    mergePolicyConfig.setPolicy(DiscardMergePolicy.class.getName());
                    break;
                case "pass_through":
                    mergePolicyConfig.setPolicy(PassThroughMergePolicy.class.getName());
                    break;
                case "expiration_time":
                    mergePolicyConfig.setPolicy(ExpirationTimeMergePolicy.class.getName());
                    break;
                case "higher_hits":
                    mergePolicyConfig.setPolicy(HigherHitsMergePolicy.class.getName());
                    break;
                case "latest_update":
                    mergePolicyConfig.setPolicy(LatestUpdateMergePolicy.class.getName());
                    break;
                case "latest_access":
                    mergePolicyConfig.setPolicy(LatestAccessMergePolicy.class.getName());
                    break;
                case "put_if_absent":
                default:
                    mergePolicyConfig.setPolicy(PutIfAbsentMergePolicy.class.getName());
                    break;
            }
        }

        return new MapConfig()
            .setName(mapName)
            .setMergePolicyConfig(mergePolicyConfig)
            .setMaxIdleSeconds((int) timeoutSeconds)
            .setBackupCount(cluster.getBackupCount())
            .setAsyncBackupCount(cluster.getAsyncBackupCount())
            .setEvictionConfig(evictionConfig);
    }
}
