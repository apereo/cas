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
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.ManagementCenterConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizePolicy;
import com.hazelcast.config.MergePolicyConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NamedConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.PartitionGroupConfig;
import com.hazelcast.config.ReplicatedMapConfig;
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
     * Sets config map.
     *
     * @param mapConfig the map config
     * @param config    the config
     */
    public static void setConfigMap(final NamedConfig mapConfig, final Config config) {
        if (mapConfig instanceof MapConfig) {
            config.addMapConfig((MapConfig) mapConfig);
        } else if (mapConfig instanceof ReplicatedMapConfig) {
            config.addReplicatedMapConfig((ReplicatedMapConfig) mapConfig);
        }
    }

    /**
     * Build config.
     *
     * @param hz        the hz
     * @param mapConfig the map config
     * @return the config
     */
    public static Config build(final BaseHazelcastProperties hz, final NamedConfig mapConfig) {
        val config = build(hz);
        setConfigMap(mapConfig, config);
        return finalizeConfig(config, hz);
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

        config.setLicenseKey(hz.getCore().getLicenseKey());
        if (cluster.getCore().getCpMemberCount() > 0) {
            config.getCPSubsystemConfig().setCPMemberCount(cluster.getCore().getCpMemberCount());
        }

        buildManagementCenterConfig(hz, config);

        val networkConfig = new NetworkConfig()
            .setPort(cluster.getNetwork().getPort())
            .setPortAutoIncrement(cluster.getNetwork().isPortAutoIncrement());

        if (StringUtils.hasText(cluster.getNetwork().getNetworkInterfaces())) {
            networkConfig.getInterfaces().setEnabled(true);
            StringUtils.commaDelimitedListToSet(cluster.getNetwork().getNetworkInterfaces())
                .forEach(faceIp -> networkConfig.getInterfaces().addInterface(faceIp));
        }

        if (StringUtils.hasText(cluster.getNetwork().getLocalAddress())) {
            config.setProperty(BaseHazelcastProperties.HAZELCAST_LOCAL_ADDRESS_PROP, cluster.getNetwork().getLocalAddress());
        }
        if (StringUtils.hasText(cluster.getNetwork().getPublicAddress())) {
            config.setProperty(BaseHazelcastProperties.HAZELCAST_PUBLIC_ADDRESS_PROP, cluster.getNetwork().getPublicAddress());
            networkConfig.setPublicAddress(cluster.getNetwork().getPublicAddress());
        }

        cluster.getNetwork().getOutboundPorts().forEach(networkConfig::addOutboundPortDefinition);

        if (cluster.getWanReplication().isEnabled()) {
            if (!StringUtils.hasText(hz.getCore().getLicenseKey())) {
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
        config.getSerializationConfig().setEnableCompression(hz.getCore().isEnableCompression());

        val instanceName = StringUtils.hasText(cluster.getCore().getInstanceName())
            ? cluster.getCore().getInstanceName()
            : UUID.randomUUID().toString();
        LOGGER.trace("Configuring Hazelcast instance name [{}]", instanceName);
        return config.setInstanceName(instanceName)
            .setProperty(BaseHazelcastProperties.HAZELCAST_DISCOVERY_ENABLED_PROP, BooleanUtils.toStringTrueFalse(cluster.getDiscovery().isEnabled()))
            .setProperty(BaseHazelcastProperties.IPV4_STACK_PROP, String.valueOf(cluster.getNetwork().isIpv4Enabled()))
            .setProperty(BaseHazelcastProperties.LOGGING_TYPE_PROP, cluster.getCore().getLoggingType())
            .setProperty(BaseHazelcastProperties.MAX_HEARTBEAT_SECONDS_PROP, String.valueOf(cluster.getCore().getMaxNoHeartbeatSeconds()));
    }

    private static void buildManagementCenterConfig(final BaseHazelcastProperties hz, final Config config) {
        val managementCenter = new ManagementCenterConfig();
        LOGGER.trace("Enables management center scripting: [{}]", hz.getCore().isEnableManagementCenterScripting());
        managementCenter.setScriptingEnabled(hz.getCore().isEnableManagementCenterScripting());

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

    private static JoinConfig createDiscoveryJoinConfig(final Config config, final HazelcastClusterProperties cluster,
                                                        final NetworkConfig networkConfig) {
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
            .setEnabled(cluster.getNetwork().isTcpipEnabled())
            .setMembers(cluster.getNetwork().getMembers())
            .setConnectionTimeoutSeconds(cluster.getCore().getTimeout());
        LOGGER.trace("Created Hazelcast TCP/IP configuration [{}] for members [{}]", tcpIpConfig, cluster.getNetwork().getMembers());

        val multicast = cluster.getDiscovery().getMulticast();
        val multicastConfig = new MulticastConfig().setEnabled(multicast.isEnabled());
        if (multicast.isEnabled()) {
            LOGGER.debug("Created Hazelcast Multicast configuration [{}]", multicastConfig);
            multicastConfig.setMulticastGroup(multicast.getGroup());
            multicastConfig.setMulticastPort(multicast.getPort());

            val trustedInterfaces = StringUtils.commaDelimitedListToSet(multicast.getTrustedInterfaces());
            if (!trustedInterfaces.isEmpty()) {
                multicastConfig.setTrustedInterfaces(trustedInterfaces);
            }
            multicastConfig.setMulticastTimeoutSeconds(multicast.getTimeout());
            multicastConfig.setMulticastTimeToLive(multicast.getTimeToLive());
        } else {
            LOGGER.debug("Skipped Hazelcast Multicast configuration since feature is disabled");
        }

        return new JoinConfig()
            .setMulticastConfig(multicastConfig)
            .setTcpIpConfig(tcpIpConfig);
    }

    private static Config finalizeConfig(final Config config, final BaseHazelcastProperties hz) {
        if (StringUtils.hasText(hz.getCluster().getCore().getPartitionMemberGroupType())) {
            val partitionGroupConfig = config.getPartitionGroupConfig();
            val type = PartitionGroupConfig.MemberGroupType.valueOf(
                hz.getCluster().getCore().getPartitionMemberGroupType().toUpperCase());
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
    public static NamedConfig buildMapConfig(final BaseHazelcastProperties hz, final String mapName, final long timeoutSeconds) {
        val cluster = hz.getCluster();

        val evictionPolicy = EvictionPolicy.valueOf(cluster.getCore().getEvictionPolicy());

        val evictionConfig = new EvictionConfig();
        evictionConfig.setEvictionPolicy(evictionPolicy);
        evictionConfig.setMaxSizePolicy(MaxSizePolicy.valueOf(cluster.getCore().getMaxSizePolicy()));
        evictionConfig.setSize(cluster.getCore().getMaxSize());

        val mergePolicyConfig = new MergePolicyConfig();
        if (StringUtils.hasText(cluster.getCore().getMapMergePolicy())) {
            switch (cluster.getCore().getMapMergePolicy().trim().toLowerCase()) {
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

        if (cluster.getCore().isReplicated()) {
            return new ReplicatedMapConfig()
                .setName(mapName)
                .setStatisticsEnabled(true)
                .setAsyncFillup(cluster.getCore().isAsyncFillup())
                .setInMemoryFormat(InMemoryFormat.BINARY)
                .setSplitBrainProtectionName(mapName.concat("-SplitBrainProtection"))
                .setMergePolicyConfig(mergePolicyConfig);
        }
        return new MapConfig()
            .setName(mapName)
            .setStatisticsEnabled(true)
            .setMergePolicyConfig(mergePolicyConfig)
            .setMaxIdleSeconds((int) timeoutSeconds)
            .setBackupCount(cluster.getCore().getBackupCount())
            .setAsyncBackupCount(cluster.getCore().getAsyncBackupCount())
            .setEvictionConfig(evictionConfig);
    }
}
